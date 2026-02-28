import cv2
import numpy as np
import random
import os
from pathlib import Path

def save_yolo_label(dst_pts, img_w, img_h, class_id, label_path, img_num):
    # 1. Find the min/max X and Y from our 4 corner points
    x_coords = dst_pts[:, 0]
    y_coords = dst_pts[:, 1]
    
    xmin, xmax = np.min(x_coords), np.max(x_coords)
    ymin, ymax = np.min(y_coords), np.max(y_coords)
    
    # 2. Calculate YOLO values (Center X, Center Y, Width, Height)
    width = (xmax - xmin)
    height = (ymax - ymin)
    x_center = xmin + (width / 2)
    y_center = ymin + (height / 2)
    
    # 3. Normalize values by image dimensions
    norm_x = x_center / img_w
    norm_y = y_center / img_h
    norm_w = width / img_w
    norm_h = height / img_h
    
    # 4. Save to .txt file
    with open(label_path + ".txt", 'w') as f:
        f.write(f"{class_id} {norm_x:.6f} {norm_y:.6f} {norm_w:.6f} {norm_h:.6f}\n")

# --- Usage inside the generation loop ---
# class_id = 0 for 'FireDragon', 1 for 'WaterKnight', etc.
# save_yolo_label(dst_pts, w_bg, h_bg, class_id, "output/img_001.txt")

def generate_synthetic_image(card_img_path, bg_img_path, output_path, id, label_path, img_num):
    # Load images
    card = cv2.imread(card_img_path, cv2.IMREAD_UNCHANGED) # Use UNCHANGED for alpha channel
    bg = cv2.imread(bg_img_path)
    
    h_bg, w_bg = bg.shape[:2]
    h_card, w_card = card.shape[:2]

    # 1. Define 4 points of the card and 4 random points on the background
    # This creates the "perspective" (tilting the card)
    src_pts = np.float32([[0, 0], [w_card, 0], [w_card, h_card], [0, h_card]])
    
    # Randomly pick where the 4 corners of the card will land on the table
    # We add random "jitter" to simulate different angles
    center_x, center_y = random.randint(200, w_bg-200), random.randint(200, h_bg-200)
    size = random.randint(150, 300)
    
    dst_pts = np.float32([
        [center_x - size + random.randint(-50,50), center_y - size + random.randint(-50,50)],
        [center_x + size + random.randint(-50,50), center_y - size + random.randint(-50,50)],
        [center_x + size + random.randint(-50,50), center_y + size + random.randint(-50,50)],
        [center_x - size + random.randint(-50,50), center_y + size + random.randint(-50,50)]
    ])

    # 2. Warp the card to fit the new perspective
    matrix = cv2.getPerspectiveTransform(src_pts, dst_pts)
    warped_card = cv2.warpPerspective(card, matrix, (w_bg, h_bg))

    # 3. Create a mask and blend (Simplified)
    # This places the warped card on top of the background
    mask = np.where(warped_card > 0, 255, 0).astype(np.uint8)
    bg_with_card = np.where(mask > 0, warped_card, bg)

    cv2.imwrite(output_path + ".jpg", bg_with_card) # Store the card

    height, width = bg_with_card.shape[:2]
    
    # 4. Generate Bounding Box for labels
    # (Calculate min/max X and Y from dst_pts to get YOLO coords)
    save_yolo_label(dst_pts, width, height, id, label_path, img_num)

if __name__ == "__main__":
    card_img_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\source_cards"  # Path to the card images
    bg_img_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\backgrounds"  # Path to the background images
    output_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\generated_dataset\\images\\generated_dataset_"  # Path to save the generated images
    label_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\generated_dataset\\yolo_labels\\yolo_labels_"  # Path to save the labels
    Card_IMG_Path = Path(card_img_path)
    BG_IMG_Path = Path(bg_img_path)
    num_images = 0
    id = 0 # Cards will be id'd by the order they are in the directory which we will coordinate with the JSON data later.
    for card in Card_IMG_Path.iterdir():
        for bg in BG_IMG_Path.iterdir():
            generate_synthetic_image(card, bg, output_path + str(num_images), id, label_path + str(num_images), num_images)
            num_images += 1 # Update the number of images created
        id +=1 # Update ID

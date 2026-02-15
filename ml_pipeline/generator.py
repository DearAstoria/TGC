import cv2
import numpy as np
import random
import os

def generate_synthetic_image(card_img_path, bg_img_path, output_path):
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

    cv2.imwrite(output_path, bg_with_card)
    
    # 4. Generate Bounding Box for labels
    # (Calculate min/max X and Y from dst_pts to get your YOLO coords)

if __name__ == "__main__":
    card_img_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\source_cards"  # Path to the card images
    bg_img_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\backgrounds"  # Path to the background images
    output_path = "C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\generated_dataset"  # Path to save the generated images

    generate_synthetic_image(card_img_path, bg_img_path, output_path)
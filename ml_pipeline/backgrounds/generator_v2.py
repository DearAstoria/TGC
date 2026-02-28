import cv2
import numpy as np
import random
import os
from pathlib import Path

def apply_artificial_cruddyness(img):
    # 1. Random Lighting Jitter (Simulate different room brightness)
    brightness = random.randint(-40, 40)
    img = cv2.convertScaleAbs(img, beta=brightness)
    
    # 2. Random Gaussian Blur (Simulate shaky hands/out-of-focus camera)
    if random.random() > 0.7: # 30% chance of blur
        k_size = random.choice([3, 5])
        img = cv2.GaussianBlur(img, (k_size, k_size), 0)
    
    # 3. Random Salt & Pepper Noise (Simulate sensor grain)
    if random.random() > 0.8:
        noise = np.zeros(img.shape, np.uint8)
        cv2.randn(noise, (0,0,0), (20,20,20))
        img = cv2.add(img, noise)
        
    return img

def save_yolo_label(dst_pts, img_w, img_h, class_id, label_full_path):
    # Clip points to image boundaries to prevent labels > 1.0
    x_coords = np.clip(dst_pts[:, 0], 0, img_w)
    y_coords = np.clip(dst_pts[:, 1], 0, img_h)
    
    xmin, xmax = np.min(x_coords), np.max(x_coords)
    ymin, ymax = np.min(y_coords), np.max(y_coords)
    
    width = (xmax - xmin) / img_w
    height = (ymax - ymin) / img_h
    x_center = (xmin + (xmax - xmin) / 2) / img_w
    y_center = (ymin + (ymax - ymin) / 2) / img_h
    
    with open(label_full_path, 'w') as f:
        f.write(f"{class_id} {x_center:.6f} {y_center:.6f} {width:.6f} {height:.6f}\n")

def generate_synthetic_image(card_path, bg_path, base_out_dir, class_id, img_num):
    # Determine if this goes to train or val (80/20 split)
    split = "train" if random.random() < 0.8 else "val"
    
    # Load images
    card = cv2.imread(str(card_path), cv2.IMREAD_UNCHANGED)
    bg = cv2.imread(str(bg_path))
    if bg is None or card is None: return # Skip if error
    
    h_bg, w_bg = bg.shape[:2]
    h_card, w_card = card.shape[:2]

    # 1. Perspective Transformation (Random Angles)
    src_pts = np.float32([[0, 0], [w_card, 0], [w_card, h_card], [0, h_card]])
    
    center_x, center_y = random.randint(300, w_bg-300), random.randint(300, h_bg-300)
    size = random.randint(180, 350)
    
    # This creates the unique "Tilt" for each of the 10 variations
    dst_pts = np.float32([
        [center_x - size + random.randint(-80,80), center_y - size + random.randint(-80,80)],
        [center_x + size + random.randint(-80,80), center_y - size + random.randint(-80,80)],
        [center_x + size + random.randint(-80,80), center_y + size + random.randint(-80,80)],
        [center_x - size + random.randint(-80,80), center_y + size + random.randint(-80,80)]
    ])

    matrix = cv2.getPerspectiveTransform(src_pts, dst_pts)
    warped_card = cv2.warpPerspective(card, matrix, (w_bg, h_bg))

    # 2. Mask and Blend
    # vv This version is not working due to broadcasting issues with the alpha channel. The simpler version below works fine
    # mask = np.where(warped_card > 0, 255, 0).astype(np.uint8)
    # # Extract just the color channels if card has Alpha
    # if warped_card.shape[2] == 4:
    #     warped_card = warped_card[:,:,:3]
    #     mask = mask[:,:,0] # Take one channel for the mask
    
    # bg_with_card = np.where(mask[:,:,None] > 0, warped_card, bg)
    mask = np.where(warped_card > 0, 255, 0).astype(np.uint8)
    bg_with_card = np.where(mask > 0, warped_card, bg)

    # 3. Add Artificial Shittiness (Lighting/Blur)
    final_img = apply_artificial_cruddyness(bg_with_card)

    # 4. Set paths and save
    img_filename = f"card_{img_num}.jpg"
    txt_filename = f"card_{img_num}.txt"
    
    img_save_path = os.path.join(base_out_dir, "images", split, img_filename)
    lbl_save_path = os.path.join(base_out_dir, "labels", split, txt_filename)
    
    cv2.imwrite(img_save_path, final_img)
    save_yolo_label(dst_pts, w_bg, h_bg, class_id, lbl_save_path)

if __name__ == "__main__":
    # Setup Paths
    base_dir = Path("C:/Users/Jaden/AndroidStudioProjects/TGCARDistributedMLTracker_Daddy/ml_pipeline")
    src_cards = base_dir / "source_cards"
    backgrounds = base_dir / "backgrounds"
    output_dir = base_dir / "generated_dataset"

    # Creates YOLO folder structure in case they don't already exist
    for s in ["train", "val"]:
        os.makedirs(output_dir / "images" / s, exist_ok=True)
        os.makedirs(output_dir / "labels" / s, exist_ok=True)

    img_count = 0
    card_id = 0
    
    # The Loop: 10 cards * 100 backgrounds * 10 variations = 10,000 images
    card_list = [f for f in src_cards.iterdir() if f.is_file()]
    bg_list = [f for f in backgrounds.iterdir() if f.is_file()]

    print(f"Starting generation for {len(card_list)} cards and {len(bg_list)} backgrounds...")

    for card_path in card_list:
        for bg_path in bg_list:
            # Add the 10 random variations per pair
            for v in range(10):
                generate_synthetic_image(card_path, bg_path, output_dir, card_id, img_count)
                img_count += 1
                
        print(f"Finished Card ID: {card_id} ({card_path.name})")
        card_id += 1

    print(f"Successfully generated {img_count} images in {output_dir}")
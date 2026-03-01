from ultralytics import YOLO

def train_TGC():
    # 1. We are loading a pre-trained "Nano" model (optimized for mobile)
    # We use 'n' for Nano because it has the lowest latency on Android
    model = YOLO('yolov8n.pt') 

    # 2. Train the model
    # data='data.yaml' tells the script where the images are.
    results = model.train(
        data='data.yaml', 
        epochs=50,        # 50-100 is enough for synthetic data
        imgsz=640,        # Standard resolution for card detection
        device='cpu',         # Using 0 for GPU (NVIDIA) or could use 'cpu'
        plots=True
    )

    # 3. Exporting the model to TFLite for the Android App
    # int8=True quantizes the model, making it 4x smaller and faster
    # Added the data argument since it didn't know about the yaml file during export which is needed for a more precise calibration.
    model.export(format='tflite', int8=True, data='C:\\Users\\Jaden\\AndroidStudioProjects\\TGCARDistributedMLTracker_Daddy\\ml_pipeline\\data.yaml')

if __name__ == "__main__":
    train_TGC()
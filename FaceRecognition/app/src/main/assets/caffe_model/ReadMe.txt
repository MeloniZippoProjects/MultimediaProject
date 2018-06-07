This directory contains four models based on Caffe [1] implementation. MTCNN [2] is used for face detection. 
This bounding box is then extended by a factor 0.3 (except the extension outside image) to include the whole head, 
which is used as the input for networks (it's worth noting that this version is a bit tighter than the released one 
where the bounding box is extended by a factor 1.0).

Durining training, a region of 224x224 pixels is randomly cropped from each input, whose shorter size is resized to 256. 
The mean value of each channel is substracted for each pixel. The provided mean vector in the prototxt is in BGR order. 
You don't need to permute if you use opecv-based implementation. Bilinear interpolation is used for image resizing. 
More details can be found in the paper of VGGFace2.

Models:

1. resnet50_scratch_caffe: ResNet-50 model trained on VGGFace2 training set from scratch.

2. resnet50_ft_caffe: ResNet-50 model fine-tuned on VGGFace2 training set based on a pretrained model on Ms-Celeb-1M dataset.

3. senet50_scratch_caffe: SE-ResNet-50 model [3] trained on VGGFace2 training set from scratch.

4. senet50_ft_caffe: SE-ResNet-50 model [3] fine-tuned on VGGFace2 training set based on a pretrained model on Ms-Celeb-1M dataset.


References for implementation:

[1] Caffe: https://github.com/BVLC/caffe

[2] MTCNN: https://github.com/kpzhang93/MTCNN_face_detection_alignment

[3] Squeeze-and-Excitation Networks: https://github.com/hujie-frank/SENet

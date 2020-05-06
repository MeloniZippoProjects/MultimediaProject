# Introduction
This is an Android app that performs face recognition and identification through the smartphone's camera.
The app uses an internal database containing the known identities, including sample photos.
The app allows the user to check the database content, delete or add new identities.

# Implementation overview
* Face detection using Haar Cascade Classifier
* Face features exctracted using [VGG2](http://www.robots.ox.ac.uk/~vgg/data/vgg_face/)
* Classification performed with weighted kNN

[OpenCV](https://opencv.org/platforms/android/) library was used for data processing.

For more details, see the presentation.

# Cannot find the caffe model?
If you get errors similar to `Can't open "/data/user/0/melonizippo.org.facerecognition/cache/caffe_model/resnet50_ft_caffe/resnet50_ft.caffemodel"`, then you must first extract the 7z multipart file called `resnet50_ft.7z.***` in the path `src/main/assets/caffe_model/resnet50_ft_caffe/`.

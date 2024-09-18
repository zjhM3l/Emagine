# NFT-Like Image Processing and Asset Sharing Community

This project is an "NFT-like community" that integrates image processing and image asset sharing transactions. Users can upload images of their own creation or rework them with a high degree of freedom using various tools. The program ensures that all image assets within the community are unique and tradable. Each asset has one owner, who holds the unique key to that asset and has the right to trade or exhibit it within the community.

## 1. Progress during Alpha

1. Implemented login registration and related functions.
2. Implemented basic image style rendering based on matrix operation without external libraries (Basic Image Process).
3. Implemented a series of image processing methods based on OpenCV with high degrees of freedom.
4. Enabled reading and uploading of local storage images.
5. Realized homepage flow display.
6. Enabled browsing of owned image assets.
7. Implemented different screen size adaptability for all page layouts.
8. Implemented LitePal database related operations.
9. Realized real-time image rendering using OpenCV Camera.
10. Preliminary implementation of the image asset details page.

## 2. Description

### 2.1 Dependencies
- **LitePal 3.0.0**: Database management.
- **OpenCV for Android SDK 4.4.0**: Used for high-end image processing and real-time rendering with a camera (code written based on official documentation).
- **MagicIndicator 1.6.0**: Optimized the horizontal slide function.
- **Facebook Fresco 0.14.1**: Optimized the appearance of image cards.

### 2.2 Main Activities

#### 2.2.1 Basic Image Process
- Written in pure Java code.
- Users can select a local image, render it, and preview it.
- Users can adjust hue, saturation, and grayscale using sliders.
- Upon uploading, the system compares the image hash to the database, and if the image is unique, it will be uploaded.

#### 2.2.2 Advanced Image Process with OpenCV
- Advanced image processing is based on OpenCV.
- Users can choose various rendering modes (e.g., grayscale, dilate, median blur, Gaussian, Canny scan, corrosion, binary).
- Features facial recognition and ROI box selection (in development).
- After rendering, the system checks the image hash for uniqueness before uploading.

#### 2.2.3 Login & Register
- Login checks existing account credentials, with an option to remember the last logged-in user.
- Registration checks for username availability and password confirmation.
- User session information is saved post-login.

#### 2.2.4 Front Page
- First page after login.
- Includes a function marquee and a waterfall-style display of images.

#### 2.2.5 Art Info
- Displays details of an image when clicked on.
- Future features include options for following authors, applying for transactions, and adding favorites.

#### 2.2.6 OpenCV Camera
- Real-time rendering camera supporting multiple rendering modes.

#### 2.2.7 Cards
- User collections displayed as swipeable stacked cards.

### 2.3 Main Tools

#### 2.3.1 Img Transfer Tools
- **ImgHelper & Bytes2Bitmap**: Facilitates image type conversions (e.g., byte, byte[], drawable, Bitmap) for rendering, processing, and display.

#### 2.3.2 Hash Tools
- **HashUtil**: Implements a Hash Distance algorithm to calculate image similarity and ensure asset uniqueness.

#### 2.3.3 Basic Image Templates Matrices
- **BeautyUtil**: Encapsulates various rendering templates for adjusting hue, saturation, and grayscale. Triggered when users interact with sliders.

#### 2.3.4 Card Swipe Tools
- **RoundImageView**, **CardConfig**, **CardItemTouchHelperCallback**, **CardLayoutManager**, **OnSwipeListener**: Enhances user experience for browsing image assets by enabling smooth card swiping animations.

#### 2.3.5 Horizontal Swiped Topic (Taobao-like)
- **TopicActivity**, **HomeTopicPagerAdapter**, **TopicAdapter**, **TopicBean**: Implements a horizontal sliding function inspired by the Taobao mobile app, with improvements for user experience.

#### 2.3.6 PopUp Window Tools
- Pop-up windows used for various user interactions, such as image uploads, with plans for further improvements.

### 2.4 Usage of OpenCV
- The program uses **OpenCV 4.4.0** for image processing and real-time rendering.
- Methods implemented: grayscale, dilate, median blur, Gaussian blur, Canny scan, corrosion, and binary rendering.
- The ROI tool allows local selection and rendering of parts of an image, with future plans to expand its functionality.
- Facial recognition is based on the European face dataset, with future enhancements planned for OpenCV Camera development.

---

## License
This project is licensed under the MIT License.


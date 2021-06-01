# SuorcApp - Sudoku OCR Application
### This is a simple Android App that detects a Sudoku in an image and Solve it.


## Steps precessing image

- [x] Convert the image to gray scale.

- [x]  Apply blur to the image.

- [x]   Apply adaptiveThreshold to find curves.

 - [x]  Find all the curves that appear in the image.

 - [x]  Find the curves that connect to each other and form the biggest area between all the curves.

 - [x]  Crop Sudoku using the outline of your grid.

 - [x]  Find the position of the numbers in the Sudoku grids.

 - [x]  Use tensorflow to identify the numbers.

 - [ ]  User can click on the image to zoom it.

## Download OpenCv

Download **openCv 4.5.1** or later

https://opencv.org/releases/

## Import the OpenCv SDK module to the project

**1** - Open Android Studio (This steps was used in **Android Stuio 4.1.3**)
**2** - File -> New -> Import Module
**3** - Go to where you have downloaded the OpenCV and select the directory **SDK**
**4** - Click Finish

**Adding the OpenCv Module to your project:**
**1** - File -> Project Structure
**2** - (1) Click on Dependencies -> select your project(My is **app**) - > (3) click on the plus button -> (4) click on Module Dependency

![Image of Yaktocat](/imgs/AddingOpenCVModuleToYourProject.png)

**3** - Select the SDK of the OpenCv and Click on the Ok button.


# SuorcApp detecting grid of the Sudoku
![Image of Yaktocat](/imgs/showAppWorking.gif)

## License
[MIT](https://choosealicense.com/licenses/mit/)

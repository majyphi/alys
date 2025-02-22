# aLys
An extensive Stock Management system for [Foxhole](https://store.steampowered.com/app/505460/Foxhole/)

## How?

Developed in Scala, it uses Discord as its main interaction medium. Users can upload screenshots of stockpiles & define production objectives and priorities.

Then, through the use of OpenCV, "aLys" performs OCR to recognize existing items and compare them with objectives to calculate production goals.
The system is able to calculate the need in raw and intermediary materials in order to reach the objectives for finished goods.
Officers can therefore define objectives solely in terms of finished goods.

## Libraries

With the help of:
- OpenCV: Open Computer Vision
- Ackcord: Discord client based on Akka
- Breeze: Vector/Matrix operations library
- Slick: ORM for Database interactions
- sqlite: Database for persistence

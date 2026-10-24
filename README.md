# Falafel Shop Cashier

A Java Swing desktop application for running a falafel shop counter: build orders, apply discounts, take payments and print receipts.

## Features
- Menu items and order building (dine-in / takeaway)
- Discounts, multiple payment methods and order status tracking
- Receipt generation

## Structure
`src/falafelshop/` contains `Main`, `MainFrame` (GUI), `CashRegister`, `OrderManager`, `Order`/`OrderItem`, `Payment`, `ReceiptGenerator` and the supporting enums.

## Run
Requires a JDK on your PATH.
```bash
javac -d out src/falafelshop/*.java
java -cp out falafelshop.Main
```
On Windows, `launch_falafel_shop.bat` compiles and launches the app.

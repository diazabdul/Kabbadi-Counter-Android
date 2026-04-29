# 🏆 Kabaddi Kounter

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Room-Database-orange?style=for-the-badge&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/MVVM-Architecture-blue?style=for-the-badge" />
</p>

---

## 📖 Overview
**Kabaddi Kounter** is a specialized score-keeping application designed for the high-intensity sport of Kabaddi. Built with modern Android development standards, it provides a seamless experience for tracking points, managing team identities, and maintaining a historical record of matches.

This project was developed for the **Teknologi & Aplikasi Piranti Bergerak (IF5230)** course, Semester 2.

---

## ✨ Features

- 📈 **Dynamic Scoring**: Rapidly update scores for Team A and Team B with $+1$ and $+2$ point increments.
- 📂 **Smart Persistence**: Match history is stored locally using **Room Database**, ensuring your data is safe even after closing the app.
- 🌓 **Theming**: Full support for **Dark Mode** to reduce eye strain during night matches.
- 📤 **JSON Export**: Export your entire match history to a JSON file in your device's Downloads folder for external analysis.
- 🔄 **Real-time UI**: Uses **Two-Way Data Binding** and **LiveData** for a reactive interface that updates instantly.
- 🗑️ **History Management**: Browse past games in a clean list and delete records with a single click.

---

## 🛠️ Tech Stack
- **Language:** [Kotlin](https://kotlinlang.org/)
- **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI Components:**
  - Fragments & RecyclerView
  - Two-way Data Binding
  - Material Design 3 components
- **File System:** MediaStore API for modern Android file exports.

---

[//]: # (## 🎮 Preview)

[//]: # (| Home Screen | Match History | Settings |)

[//]: # (| :---: | :---: | :---: |)

[//]: # (| <img src="https://via.placeholder.com/200x400?text=Score+Counter" width="200" /> | <img src="https://via.placeholder.com/200x400?text=History+List" width="200" /> | <img src="https://via.placeholder.com/200x400?text=Dark+Mode" width="200" /> |)

[//]: # ()
[//]: # (---)

## 🚀 Installation
1. Clone the repository.
2. Open with **Android Studio**.
3. Sync Gradle and build the project.
4. Run on an emulator or physical device (API 24+).

---

## 🎓 Academic Context
This project implements requirements from the [IF5230 Technical Presentation](https://docs.google.com/presentation/d/10Qpt3z3-DASD-9AmADRBD_7AElM_5cLH4yf2HKgl4YY/edit#slide=id.gb8b649eef8_0_411).

<p align="center">
  Developed with ⚡ and ☕ by <b>Your Name</b>
</p>

📚 Course Management System – Practical Exam

📝 Description
An Android application for managing courses and students, with full offline functionality and automatic synchronization with a .NET API backend.

🚀 Features
✅ Complete CRUD operations for Courses  
👥 Management of Students per Course  
📴 Offline support using Room database  
🔄 Automatic API synchronization when connection is available  
📬 Push notifications using Firebase Cloud Messaging (FCM)

🛠️ Technologies Used
🎨 Jetpack Compose – UI toolkit  
🏗️ MVVM Architecture – Separation of concerns  
🌐 Retrofit + OkHttp – API communication  
💾 Room Database – Local storage and caching  
📲 Firebase Cloud Messaging (FCM) – Notifications

📋 Requirements
💻 Android Studio Hedgehog or newer  
☕ JDK 17+  
📁 Firebase google-services.json configuration file  
📱 Emulator or physical device with Android 8.0+

⚙️ Setup Instructions
1️⃣ Clone the repository:  
   🧬 git clone git@github.com:Kevin7819/Exam-Front.git

2️⃣ Firebase Configuration:  
   🔽 Download the google-services.json file from your Firebase project.  
   📂 Place it inside the app/ directory.

3️⃣ API Configuration:  
   🌍 Base URL: http://10.0.2.2:5275/  
   ⚠️ Make sure the .NET backend is running locally if testing on an emulator.

4️⃣ Build and Run:  
   🛠️ Open the project in Android Studio  
   ⏳ Wait for Gradle sync to complete  
   ▶️ Click the green Run button to launch the app

🌐 API Endpoints

📁 Courses:
🔹 GET     /api/courses  
🔹 GET     /api/courses/{id}  
🔹 POST    /api/courses  
🔹 PUT     /api/courses/{id}  
🔹 DELETE  /api/courses/{id}  

📁 Students:
🔸 POST    /api/students  
🔸 GET     /api/students/byCourse/{courseId}  
🔸 PUT     /api/students/{id}  
🔸 DELETE  /api/students/{id}  

🧪 Testing Guidelines

📴 Offline Testing:
🚫 Disable network connection  
📋 Verify that course and student data is stored locally  
📌 Ensure operations are queued for synchronization

🌐 Online Testing:
✅ Re-enable network connection  
🔄 Confirm synchronization of pending operations  
📨 Test the reception of push notifications

📦 Dependencies
📦 androidx.room:room-runtime:2.6.0  
📦 com.squareup.retrofit2:retrofit:2.9.0  
📦 com.google.firebase:firebase-messaging:23.3.1

👨‍💻 Contributors
👤 Gerald Andrey Calderón Castillo  
🔗 GitHub: https://github.com/Gera10CC  
🆔 ID: 703050481

👤 Kevin Abel Venegas Bermúdez  
🔗 GitHub: https://github.com/Kevin7819  
🆔 ID: 703070997

📂 Related Repositories
🔗 Backend API: git@github.com:Kevin7819/Exam-Back.git

🌿 Branch
🌟 Default branch: master

📌 Rubric Checklist
✅ Complete CRUD operations  
✅ Offline Room  
✅ API synchronization (Retrofit + OkHttp)  
✅ Documentation

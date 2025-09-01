# Food Review App

## Project Overview

Food Review App is an Android-based social application for food reviews. Users can share their dining experiences, view reviews from others, and discover nearby restaurants through the integrated map feature. The app follows modern Material Design principles to provide a smooth user experience.

## Technical Architecture

- **Programming Languages**: Java + Kotlin  
- **UI Frameworks**: Jetpack Compose + XML Layouts  
- **Backend Services**: Firebase (Authentication, Firestore, Storage)  
- **Map Services**: Google Maps API  
- **Location Services**: Google Play Services Location  
- **Real-time Analytics**: Firebase Analytics for crowd density tracking  
- **Minimum SDK Version**: API 24 (Android 7.0)  
- **Target SDK Version**: API 34 (Android 14)  

## Core Features

### User Authentication System

- **User Registration**: Email-based registration and account creation  
- **User Login**: Secure authentication with Firebase  
- **Profile Management**: Update username, email, and password  
- **Secure Logout**: Safe account sign-out  

### Home

- **Post Browsing**: Image-and-text display similar to Xiaohongshu (RED)  
- **Search Function**: Keyword search bar at the top  
- **Post Details**: View complete content and reviews by clicking a post  
- **Review System**: Two modes of reviews — before dining and after dining  
- **Content Display**: Rich multimedia content with text and images  

### Map

- **Restaurant Locator**: Display geographic coordinates of nearby restaurants  
- **Interactive Map**: Google Maps-based interactive experience  
- **Location Permissions**: Smart management of location access  
- **Restaurant Information**: Tap a restaurant marker to view related reviews  
- **Review Filters**: Filter reviews by "before dining" or "after dining"  
- **Crowd Density System**: Real-time restaurant crowding status display
  - **Crowding Levels**: Three-tier system with color-coded indicators
    - 🟢 Not Crowded (Green): Comfortable dining experience
    - 🟡 Moderately Crowded (Yellow): Some waiting time expected
    - 🔴 Very Crowded (Red): Long waiting time, consider alternatives
  - **User Feedback**: Users can submit current crowding status
  - **Real-time Updates**: System processes feedback within 1-hour window
  - **Weighted Algorithm**: Time-based weighting system for accurate status
    - Recent 10 minutes: Weight 1.0 (highest priority)
    - Recent 30 minutes: Weight 0.7 (medium priority)
    - Recent 1 hour: Weight 0.3 (lower priority)
  - **Dynamic Status**: Crowding level automatically updates based on user submissions

### Profile

- **User Information**: View and edit personal details  
- **Account Settings**: Change password, update email  
- **Data Sync**: Real-time synchronization with Firebase  
- **Account Security**: Safe account operations and management  

### Content Creation

- **Post Publishing**: Create food review posts with text and images  
- **Multimedia Support**:  
  - Camera: Take photos directly in-app  
  - Gallery: Select images from local storage  
- **Voice Input**: Convert speech to text for faster input  
- **Review Association**: Posts are automatically linked to the corresponding restaurant  

## Installation and Setup

### Requirements

- Android Studio Hedgehog | 2023.1.1 or later  
- Android SDK 34  
- Java 17 or higher  
- Google Play Services  

### Build Steps

1. Clone the project repository  
2. Open the project in Android Studio  
3. Ensure required SDK components are installed  
4. Configure Firebase project (if applicable)  
5. Run the application  

### Permissions

The app requires the following permissions:  

- Internet access  
- Location services  
- Camera access  
- Storage access  

## Key Highlights

- **Modern UI**: Built with Material Design 3 principles  
- **Responsive Design**: Supports multiple screen sizes and resolutions  
- **Real-Time Data**: Firebase real-time database integration  
- **Smart Location Services**: Precise location with Google Maps  
- **Rich Multimedia Support**: Image upload and voice-to-text input  
- **User Experience**: Smooth navigation and interactive flow  
- **Crowd Intelligence**: Community-driven restaurant crowding insights  
- **Smart Analytics**: Time-weighted algorithm for accurate status updates  

## Development Status

The project is currently in the development phase, with the following components completed or in progress:  

- User authentication system  
- Basic UI framework  
- Map integration  
- Firebase backend integration  
- Post system (in development)  
- Review functionality (in development)  
- Search functionality (in development)  
- Crowd density system (planned)  
- Real-time analytics engine (planned)


name: Build DigitalTelevision APK

on:
  workflow_dispatch:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v5

      - name: Find Android project
        run: |
          echo "Searching for Android project..."
          find . -type f -name "gradlew"

      - name: Build APK
        run: |
          PROJECT=$(find . -type f -name "gradlew" | head -n 1 | xargs dirname)
          if [ -z "$PROJECT" ]; then
            echo "Android Gradle project not found"
            exit 1
          fi
          cd "$PROJECT"
          chmod +x gradlew
          ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: DigitalTelevision-APK
          path: "**/build/outputs/apk/debug/*.apk"
          if-no-files-found: error


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

      - name: Extract Android project
        run: |
          unzip -q DigitalTelevisionPlayer-source.zip -d project

      - name: Find Android project
        run: |
          find project -type f -name gradlew

      - name: Build APK
        run: |
          PROJECT=$(dirname "$(find project -type f -name gradlew | head -n 1)")
          if [ ! -f "$PROJECT/gradlew" ]; then
            echo "Android project not found"
            exit 1
          fi
          cd "$PROJECT"
          chmod +x gradlew
          ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: DigitalTelevision-APK
          path: project/**/build/outputs/apk/debug/*.apk
          if-no-files-found: error

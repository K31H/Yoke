# Yoke

An Android app for finding connecting flights. Search by city name, pick your dates on a calendar, compare nonstop and one-stop options, and open the booking page of the provider you choose.

Built for **CS32005** at **Dundee University**.

## Features

- **Flight search** by city name or airport code, with suggestions as you type
- **Calendar date picker** for departure and (optional) return dates
- **Results** with nonstop and one-stop flights, sorted by price, with duplicates merged to the cheapest fare
- **Itinerary page** showing each leg, the layover time, and the outbound and return flights on round trips
- **Booking** through the provider of your choice, opening their site in the browser
- **Saved searches** that are kept when the app is closed
- **Settings:** currency (GBP, USD, EUR), dark mode, and a home airport that can be found from your GPS location
- Airline logos, and light and dark themes

## Built with

- Kotlin and Jetpack Compose (Material 3)
- Kotlin coroutines for background work
- `HttpURLConnection` and `org.json` for networking (no third-party libraries)
- `SharedPreferences` for saved data and settings
- Android `LocationManager` for finding the nearest airport
- [Ignav API](https://ignav.com/docs) for flight search, booking links and airport search
- Airline logos from the Aviasales logo service

## Getting started

### Requirements

- Android Studio (current version)
- An Android device or emulator running Android **24** or newer
- A free API key from [Ignav](https://ignav.com/signup)

### Setup

1. Clone the repository:

   ```
   git clone https://github.com/K31H/Yoke.git
   ```

2. Open the project in Android Studio and let Gradle sync.

3. Create or edit `local.properties` in the project's root folder and add your key:

   ```
   IGNAV_API_KEY=your_key_here
   ```

   This file is ignored by Git, so your key is not uploaded. **Never commit it.**

4. Run the app on a device or emulator.

To test the nearest-airport feature on an emulator, set a location in the emulator's **Extended controls → Location** panel.

## Running the tests

The project has 40 unit tests covering date parsing, airport search, the distance calculation, flight data parsing, and price and duration formatting.

```
./gradlew testDebugUnitTest
```

Or right-click `app/src/test` in Android Studio and choose **Run Tests**. The HTML report is saved in `app/build/reports/tests/testDebugUnitTest/`.

## Project structure

| File | What it does |
|---|---|
| `MainActivity.kt` | Entry point; sets the theme (light or dark) |
| `YokeApp.kt` | Holds the app's state, handles navigation and starts searches |
| `InputScreen.kt`, `ResultsPage.kt`, `SelectedPage.kt`, `SavedSearchesPage.kt`, `SettingsPage.kt` | The screens |
| `IgnavApi.kt` | Calls the flight API and turns its JSON into flight results |
| `Airports.kt`, `AirportLookup.kt` | Built-in airport list, search, and the nearest-airport maths |
| `AirportInput.kt`, `DateField.kt`, `AirlineLogo.kt` | Reusable input and image components |
| `Storage.kt` | Saves searches and settings on the device |
| `Navigation.kt` | Bottom navigation bar and page header |
| `ui/theme/Theme.kt` | Colours, text styles and shapes |

## Known limitations

- **No date-range search or price calendar yet.** You choose one departure date and an optional return date. A range search is the main planned feature.
- Sorting and filtering options, price predictions and price alerts are not implemented.
- Only nonstop and one-stop flights are shown.
- The API key is bundled into the app at build time, so it could be extracted from the APK. A real release would send requests through a backend that holds the key.
- Saved data is stored with `SharedPreferences`, not a database.
- Strings are written directly in the code, so the app isn't set up for translation yet.

## Acknowledgements

- Flight data and booking links: [Ignav](https://ignav.com)
- Airline logos: Aviasales / Travelpayouts. The logos are trademarks of their airlines.

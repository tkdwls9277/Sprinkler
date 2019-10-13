package com.example.arduino;

public class WeatherHangeul {
    String weather;
    WeatherHangeul(String weather){
        this.weather=weather;
    }
    public String getWeather(){
        switch (weather){
            case "thunderstorm with light rain": return "번개와 보슬비";
            case "thunderstorm with rain": return "번개와 비";
            case "thunderstorm with heavy rain": return "번개와 집중 호우";
            case "light thunderstorm": return "천둥";
            case "thunderstorm": return "천둥 번개";
            case "heavy thunderstorm": return "강한 천둥 번개";
            case "ragged thunderstorm": return "매우 강한 천둥 번개";
            case "thunderstorm with light drizzle": return "번개와 가벼운 이슬비";
            case "thunderstorm with drizzle": return "번개와 이슬비";
            case "thunderstorm with heavy drizzle": return "번개와 집중 호우";

            case "light intensity drizzle": return "약한 이슬비";
            case "drizzle": return "이슬비";
            case "heavy intensity drizzle": return "강한 이슬비";
            case "light intensity drizzle rain": return "약한 이슬비";
            case "drizzle rain": return "이슬비";
            case "heavy intensity drizzle rain": return "강한 이슬비";
            case "shower rain and drizzle": return "소나기";
            case "heavy shower rain and drizzle": return "강한 소나기";
            case "shower drizzle": return "소나기";

            case "light rain": return "가벼운 비";
            case "moderate rain": return "비";
            case "heavy intensity rain": return "강한 비";
            case "very heavy rain": return "집중 호우";
            case "extreme rain": return "집중 호우";
            case "freezing rain": return "어는 비";
            case "light intensity shower rain": return "가벼운 소나기";
            case "shower rain": return "소나기";
            case "heavy intensity shower rain": return "강한 소나기";
            case "ragged shower rain": return "매우 강한 소나기";

            case "light snow": return "약한 눈";
            case "snow": return "눈";
            case "heavy snow": return "거센 눈";
            case "sleet": return "진눈깨비";
            case "shower sleet": return "급 진눈깨비";
            case "light rain and snow": return "약한 눈과 비";
            case "rain and snow": return "눈과 비";
            case "light shower snow": return "눈";
            case "shower snow": return "소낙눈";
            case "heavy shower snow": return "강한 소낙눈";

            case "mist": return "안개";
            case "smoke": return "연기";
            case "haze": return "실안개";
            case "sand, dust whirls": return "황사 바람";
            case "fog": return "안개";
            case "sand": return "황사";
            case "dust": return "황사";
            case "volcanic ash": return "화산재";
            case "squalls": return "돌풍";
            case "tornado": return "태풍";

            case "clear sky": return "맑은 하늘";
            case "few clouds": return "구름 조금";
            case "scattered clouds": return "조각 구름";
            case "broken clouds": return "조각 구름";
            case "overcast clouds": return "흐림";

            case "calm": return "바람 없음";
            case "light breeze": return "남실 바람";
            case "gentle breeze": return "산들 바람";
            case "moderate breeze": return "건들 바람";
            case "fresh breeze": return "흔들 바람";
            case "strong breeze": return "된바람";
            case "high wind, near gale": return "센바람";
            case "gale": return "강풍";
            case "severe gale": return "극심한 강풍";
            case "storm": return "폭풍우";
            case "violent storm": return "폭풍";
            case "hurricane": return "허리케인";
        }
        return "날씨 모름";
    }
}

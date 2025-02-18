/**
 * This file is part of TinyWeatherForecastGermany.
 *
 * Copyright (c) 2020, 2021 Pawel Dube
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.kaffeemitkoffein.tinyweatherforecastgermany;

import android.content.Context;
import android.content.Intent;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;

import java.util.Calendar;

public class GadgetbridgeAPI {

    public final static String WEATHER_EXTRA="WeatherSpec";
    public final static String WEATHER_ACTION="de.kaffeemitkoffein.broadcast.WEATHERDATA";

    private WeatherSpec weatherSpec;
    private Context context;
    CurrentWeatherInfo weatherCard;

    public GadgetbridgeAPI(Context context){
        this.context = context;
    }

    private void setWeatherData(){
        WeatherSettings weatherSettings = new WeatherSettings(context);
        if (weatherCard==null){
            weatherCard = new Weather().getCurrentWeatherInfo(context);
        }
        if (weatherCard!=null){
            // build the WeatherSpec instance with current weather
            weatherSpec = new WeatherSpec();
            weatherSpec.location             = weatherCard.getCity();
            // fake timestamp for current weather if desired by user; some wearables do not accept a forecast
            // for current weather.
            if (weatherSettings.gadgetbridge_fake_timestamp){
                weatherSpec.timestamp            = (int) (Calendar.getInstance().getTimeInMillis()/1000);
            } else {
                weatherSpec.timestamp            = (int) (weatherCard.currentWeather.getTimestamp() / 1000);
            }
            if (weatherCard.currentWeather.hasCondition()){
                weatherSpec.currentConditionCode = WeatherCodeContract.translateToOpenWeatherCode(weatherCard.currentWeather.getCondition());
                weatherSpec.currentCondition     = WeatherCodeContract.getWeatherConditionText(context,weatherCard.currentWeather.getCondition());
            }
            if (weatherCard.currentWeather.hasTemperature()){
                weatherSpec.currentTemp          = weatherCard.currentWeather.getTemperatureInt();
            }
            if (weatherCard.currentWeather.hasRH()){
                weatherSpec.currentHumidity      = weatherCard.currentWeather.getRHInt();
            }
            if (weatherCard.currentWeather.hasMinTemperature()){
                weatherSpec.todayMinTemp         = weatherCard.currentWeather.getMinTemperatureInt();
            }
            if (weatherCard.currentWeather.hasMaxTemperature()){
                weatherSpec.todayMaxTemp         = weatherCard.currentWeather.getMaxTemperatureInt();
            }
            if (weatherCard.currentWeather.hasWindSpeed()){
                weatherSpec.windSpeed            = (float) weatherCard.currentWeather.getWindSpeedInKmhInt();
            }
            if (weatherCard.currentWeather.hasWindDirection()){
                weatherSpec.windDirection        = (int) weatherCard.currentWeather.getWindDirection();
            }
            // build the forecast instance
            for (int i=0; i<weatherCard.forecast24hourly.size(); i++){
                // do not add and/or stop adding forecast if values are unknown
                if (!weatherCard.forecast24hourly.get(i).hasMinTemperature()||
                        (!weatherCard.forecast24hourly.get(i).hasMaxTemperature())||
                        (!weatherCard.forecast24hourly.get(i).hasCondition())||
                        (!weatherCard.forecast24hourly.get(i).hasRH())){
                    break;
                }
                WeatherSpec.Forecast forecast = new WeatherSpec.Forecast(
                        weatherCard.forecast24hourly.get(i).getMinTemperatureInt(),
                        weatherCard.forecast24hourly.get(i).getMaxTemperatureInt(),
                        WeatherCodeContract.translateToOpenWeatherCode(weatherCard.forecast24hourly.get(i).getCondition()),
                        weatherCard.forecast24hourly.get(i).getRHInt());
                weatherSpec.forecasts.add(forecast);
            }
            /*
            PrivateLog.log(context,Tag.GB,"Timestamp          : "+weatherSpec.timestamp);
            PrivateLog.log(context,Tag.GB,"Condition Code     : "+weatherSpec.currentConditionCode);
            PrivateLog.log(context,Tag.GB,"Condition          : "+weatherSpec.currentCondition);
            PrivateLog.log(context,Tag.GB,"Temperature current: "+weatherSpec.currentTemp);
            PrivateLog.log(context,Tag.GB,"Temperature min    : "+weatherSpec.todayMinTemp);
            PrivateLog.log(context,Tag.GB,"Temperature max    : "+weatherSpec.todayMaxTemp);
            PrivateLog.log(context,Tag.GB,"FC-Temperature max : "+weatherSpec.forecasts.get(0).minTemp);
            PrivateLog.log(context,Tag.GB,"FC-Temperature max : "+weatherSpec.forecasts.get(0).maxTemp);
            PrivateLog.log(context,Tag.GB,"FC-Condition       : "+weatherSpec.forecasts.get(0).conditionCode);
            PrivateLog.log(context,Tag.GB,"# of day forecasts : "+weatherSpec.forecasts.size());
            PrivateLog.log(context,Tag.GB,"Windspeed          : "+weatherSpec.windSpeed);
            PrivateLog.log(context,Tag.GB,"Windspeed direct.  : "+weatherSpec.windDirection);
            */
        }
    }

    private final void sendWeatherBroadcast(){
        WeatherSettings weatherSettings = new WeatherSettings(context);
        setWeatherData();
        if (weatherSpec!=null){
            Intent intent = new Intent();
            intent.putExtra(WEATHER_EXTRA,weatherSpec);
            // going by the docs, this requires at least api level 14
            // read the package name from the settings. Users may change the package name to
            // be able to use forks.
            intent.setPackage(weatherSettings.gadgetbridge_packagename);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setAction(WEATHER_ACTION);
            context.sendBroadcast(intent);
            PrivateLog.log(context,PrivateLog.GB,PrivateLog.INFO,"Sent weather broadcast to GadgetBridge:");
            PrivateLog.log(context,PrivateLog.GB,PrivateLog.INFO,"+-> package name: "+weatherSettings.gadgetbridge_packagename);
        } else {
            PrivateLog.log(context,PrivateLog.GB,PrivateLog.ERR,"GadgetBridge could not be served because there is no weather data.");
        }
    }

    public final void sendWeatherBroadcastIfEnabled(){
        WeatherSettings weatherSettings = new WeatherSettings(context);
        if (weatherSettings.serve_gadgetbridge){
            sendWeatherBroadcast();
        }
    }

    public final void sendWeatherBroadcastIfEnabled(CurrentWeatherInfo currentWeatherInfo){
        this.weatherCard = currentWeatherInfo;
        sendWeatherBroadcastIfEnabled();
    }

}



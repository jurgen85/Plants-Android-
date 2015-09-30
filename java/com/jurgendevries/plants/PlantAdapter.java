package com.jurgendevries.plants;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Jurgen on 29-3-2015.
 */
public class PlantAdapter extends ArrayAdapter<ParseObject> {
    public static final String TAG = PlantAdapter.class.getSimpleName();
    protected Context mContext;
    protected List<ParseObject> mPlants;
    protected TextView mTimeTicker;
    protected ParseObject mPlant;
    protected long mNewTime;
    protected long mNewWaterTime;
    protected long mNewResettedTime;

    public PlantAdapter (Context context, List<ParseObject> plants) {
        super(context, R.layout.plant_item, plants);
        mContext = context;
        mPlants = plants;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.plant_item, null);
            holder = new ViewHolder();
            holder.thumbImageView = (ImageView) convertView.findViewById(R.id.plantThumb);
            holder.plantNameLabel = (TextView) convertView.findViewById(R.id.plantNameLabel);
            holder.plantTimerReset = (Button) convertView.findViewById(R.id.plantTimerReset);
            holder.plantTimerLabel = (TextView) convertView.findViewById(R.id.plantTimerLabel);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        mPlant = mPlants.get(position);

        ParseFile plantThumb = mPlant.getParseFile(ParseConstants.KEY_IMAGE_FILE);
        Uri plantThumbUri = Uri.parse(plantThumb.getUrl());

        Picasso.with(mContext).load(plantThumbUri).transform(new RoundedTransformation(240, 0)).into(holder.thumbImageView);
        holder.plantNameLabel.setText(mPlant.getString(ParseConstants.KEY_PLANT_NAME));
        long now = System.currentTimeMillis();
        long timeLeft = mPlant.getLong(ParseConstants.KEY_WATER_TIME) - now;
        final SimpleDateFormat formatter = new SimpleDateFormat(mContext.getString(R.string.time_formatter));

        int timeAsInt = (int) timeLeft;
        if (timeAsInt > 0) {
            holder.plantTimerLabel.setText(formatter.format(timeLeft));
            holder.plantTimerReset.setVisibility(View.INVISIBLE);
        } else {
            holder.plantTimerReset.setText("reset");
            holder.plantTimerReset.setTag(mPlant.getObjectId());

        }

        holder.plantTimerReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plantId = (String)v.getTag();
                String occurence = mPlant.getString(ParseConstants.KEY_OCCURRENCE);
                mNewWaterTime = calcNewWaterTime(occurence);
                mNewResettedTime = System.currentTimeMillis();

                updatePlantTimer(plantId);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView thumbImageView;
        TextView plantNameLabel;
        TextView plantTimerLabel;
        Button plantTimerReset;
    }

    protected long calcNewWaterTime(String occurrency) {
        mNewTime = System.currentTimeMillis();
        long dayInMills = 86400000;
        switch (occurrency) {
            case "every day":
                return mNewTime + dayInMills;
            case "every 2 days":
                return mNewTime + dayInMills * 2;
            case "every 3 days":
                return mNewTime + dayInMills * 3;
            case "every 4 days":
                return mNewTime + dayInMills * 4;
            case "every 5 days":
                return mNewTime + dayInMills * 5;
            case "every 6 days":
                return mNewTime + dayInMills * 6;
            case "every week":
                return mNewTime + dayInMills * 7;
            case "every 2 weeks":
                return mNewTime + dayInMills * 14;
            case "every 3 weeks":
                return mNewTime + dayInMills * 21;
            case "every month":
                return mNewTime + dayInMills * 30;
            case "every 2 months":
                return mNewTime + dayInMills * 60;
            case "every 3 months":
                return mNewTime + dayInMills * 90;
            case "every 6 months":
                return mNewTime + dayInMills * 180;
            case "every year":
                return mNewTime + dayInMills * 365;
        }
        return mNewTime + dayInMills;
    }

    protected void updatePlantTimer(String plantId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_PLANTS);

        // Retrieve the object by id
        query.getInBackground(plantId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject plant, ParseException e) {
                if(e == null) {
                    plant.put(ParseConstants.KEY_NEW_TIME, mNewResettedTime);
                    plant.put(ParseConstants.KEY_WATER_TIME, mNewWaterTime);
                    plant.saveInBackground();

                }
            }
        });
    }
}

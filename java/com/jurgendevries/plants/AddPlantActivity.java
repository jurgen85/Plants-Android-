package com.jurgendevries.plants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AddPlantActivity extends ActionBarActivity {

    public static final String TAG = AddPlantActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int CHOOSE_PHOTO_REQUEST = 1;

    public static final int MEDIA_TYPE_IMAGE = 2;

    protected Uri mMediaUri;
    protected Bitmap mScaledImage;
    protected int mAmountOfWater;
    protected long mWaterTime;
    protected long mNewTime;

    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case 0: //Take photo
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if (mMediaUri == null) {
                        //display error
                        Toast.makeText(AddPlantActivity.this, getString(R.string.error_external_storage),
                                Toast.LENGTH_LONG).show();
                    } else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1: //Choose photo
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, CHOOSE_PHOTO_REQUEST);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType) {
            if (isExternalStorageAvailable()) {
                // get URI

                //1. get ext storage dir
                String appName = getString(R.string.app_name);
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        appName);

                //2. create sub dir
                if (! mediaStorageDir.exists()) {
                    if(!mediaStorageDir.mkdirs()) {
                        Log.e(TAG, getString(R.string.error_message_create_photo_dir));
                    }
                }
                //3. create filename
                //4. create file
                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);

                String path = mediaStorageDir.getPath() + File.separator;
                if(mediaType == MEDIA_TYPE_IMAGE) {
                    mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                } else {
                    Log.e(TAG, "Something went wrong here!");
                    return null;
                }

                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

                //5. return file uri
                return Uri.fromFile(mediaFile);
            } else {
                return null;
            }
        }

        private boolean isExternalStorageAvailable() {
            String state = Environment.getExternalStorageState();

            if (state.equals(Environment.MEDIA_MOUNTED)) {
                return true;
            } else {
                return false;
            }
        }
    };

    @InjectView(R.id.plant_image_placeholder) ImageView mPlantHolderImage;
    @InjectView(R.id.plant_name_text) EditText mPlantNameText;
    @InjectView(R.id.save_plant_button) Button mSavePlantButton;
    @InjectView(R.id.amount_of_water_spinner) Spinner mWaterSpinner;
    @InjectView(R.id.occurrence_spinner) Spinner mOccurrenceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        ButterKnife.inject(this);

        mPlantHolderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO_REQUEST) {
                if(data == null) {
                    Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
                } else {
                    mMediaUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mMediaUri);
                        mScaledImage = Bitmap.createScaledBitmap(bitmap, 480, 480, false);
                    } catch (IOException e) {
                        Log.e(TAG, "Error: " + e);
                    }
                }
            } else {
                // add image to gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            // Show image preview
            byte[] imageFileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
            Bitmap resizedBitmap = ImageResizer.resizeImage(imageFileBytes, 480, 480, 0, 0);
            Bitmap roundedBitmap = ImageResizer.getRoundedShape(resizedBitmap, 480);
            mPlantHolderImage.setImageBitmap(roundedBitmap);

            mSavePlantButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check if all required fields are filled in
                    String plantName = mPlantNameText.getText().toString().trim();

                    if (plantName.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
                        builder.setMessage(getString(R.string.add_plant_error_message))
                                .setTitle(getString(R.string.add_plant_error_title))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        // make ParseObject Plant after filling all the required fields
                        String occurrenceSelection = mOccurrenceSpinner.getSelectedItem().toString().trim();
                        String waterSelection = mWaterSpinner.getSelectedItem().toString().trim();
                        if (plantName.isEmpty() || occurrenceSelection.isEmpty() || waterSelection.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
                            builder.setMessage(getString(R.string.missing_fields_for_new_plant_error_message))
                                    .setTitle(getString(R.string.set_plant_attributes_error_title))
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            ParseObject plant = createPlant(plantName);
                            if (plant == null) {
                                // error
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
                                builder.setMessage(getString(R.string.set_plant_attributes_error_message))
                                        .setTitle(getString(R.string.set_plant_attributes_error_title))
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                send(plant);
                                finish();
                            }
                        }
                    }
                }
            });

        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_plant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected ParseObject createPlant(String plantName) {
        ParseObject plant = new ParseObject(ParseConstants.CLASS_PLANTS);
        plant.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        plant.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        plant.put(ParseConstants.KEY_PLANT_NAME, plantName);
        plant.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_IMAGE);
        plant.put(ParseConstants.KEY_AMOUNT, mWaterSpinner.getSelectedItem().toString());
        plant.put(ParseConstants.KEY_OCCURRENCE, mOccurrenceSpinner.getSelectedItem().toString());
        mNewTime = System.currentTimeMillis();
        plant.put(ParseConstants.KEY_NEW_TIME, mNewTime);
        mWaterTime = calcNewWaterTime(mOccurrenceSpinner.getSelectedItem().toString());
        plant.put(ParseConstants.KEY_WATER_TIME, mWaterTime);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null) {
            return null;
        } else {
            byte[] imageBytes = FileHelper.reduceImageForUpload(fileBytes);

            String imageFileName = FileHelper.getImageFileName(this, mMediaUri, ParseConstants.TYPE_IMAGE);
            ParseFile imageFile = new ParseFile(imageFileName, imageBytes);
            plant.put(ParseConstants.KEY_IMAGE_FILE, imageFile);
            return plant;
        }
    }

    protected void send(ParseObject plant) {
        plant.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // succes!
                    Toast.makeText(AddPlantActivity.this, getString(R.string.plant_saved_message), Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
                    builder.setMessage(getString(R.string.error_saving_plant_message))
                            .setTitle(getString(R.string.set_plant_attributes_error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    protected long calcNewWaterTime(String occurrency) {
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
}

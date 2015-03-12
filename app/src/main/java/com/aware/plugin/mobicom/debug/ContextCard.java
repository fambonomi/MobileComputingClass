package com.aware.plugin.mobicom.debug;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.providers.Light_Provider;
import com.aware.utils.IContextCard;

/**
 * Created by denzil on 12/3/15.
 */
public class ContextCard implements IContextCard {

    public ContextCard(){}

    @Override
    public View getContextCard(Context context) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = layoutInflater.inflate(R.layout.card, null);
        TextView light = (TextView) card.findViewById(R.id.light_value);

        Cursor light_data = context.getContentResolver().query(Light_Provider.Light_Data.CONTENT_URI, null, null, null, Light_Provider.Light_Data.TIMESTAMP + " DESC LIMIT 1");
        if( light_data != null && light_data.moveToFirst() ) {
            double light_val = light_data.getDouble(light_data.getColumnIndex(Light_Provider.Light_Data.LIGHT_LUX));
            light.setText(light_val + " lux");
        }
        if( light_data != null && ! light_data.isClosed()) light_data.close();

        return card;
    }
}

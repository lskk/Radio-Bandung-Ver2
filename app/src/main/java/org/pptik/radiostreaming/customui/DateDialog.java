package org.pptik.radiostreaming.customui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import org.pptik.radiostreaming.R;

import java.util.Calendar;

/**
 * Created by Hafid on 4/21/2017.
 */

public class DateDialog extends DialogFragment {

    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private EditText textDate;

    public DateDialog(){};

    @SuppressLint("ValidFragment")
    public DateDialog(View view)  {
        textDate = (EditText) view;
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),onDateSetListener, year, month, day);
        return dialog;
    }

    public void setDate(int year, int month, int day) {
        String date = day+"-"+month+"-"+year;
        textDate.setText(date);
    }
}
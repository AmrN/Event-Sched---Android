package amr.frost.com.eventschedulingsystemfory_telecom;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by pc on 8/24/2015.
 */
public class FilterDialog extends Dialog
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private AddEventActivity mActivity;
    private CheckBox cbLimitDaysOfWeek, cbLimitTimeOfDay, cbLimitDate;
    private ViewGroup vgLimitDaysOfWeek, vgLimitTimeOfDay, vgLimitDate;
    private Button btnTimeOfDayFrom, btnTimeOfDayTo, btnDateFrom, btnDateTo;

    public FilterDialog(AddEventActivity context) {
        super(context);
        mActivity = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_filter);

        cbLimitDaysOfWeek = (CheckBox) findViewById(R.id.cbLimitDaysOfWeek);
        cbLimitTimeOfDay = (CheckBox) findViewById(R.id.cbLimitTimeOfDay);
        cbLimitDate = (CheckBox) findViewById(R.id.cbLimitDate);

        cbLimitDaysOfWeek.setOnCheckedChangeListener(this);
        cbLimitTimeOfDay.setOnCheckedChangeListener(this);
        cbLimitDate.setOnCheckedChangeListener(this);

        vgLimitDaysOfWeek = (ViewGroup) findViewById(R.id.containerDaysOfWeek);
        vgLimitTimeOfDay = (ViewGroup) findViewById(R.id.containerTimeOfDay);
        vgLimitDate = (ViewGroup) findViewById(R.id.containerLimitDate);

        btnTimeOfDayFrom = (Button) findViewById(R.id.btnTimeOfDayFrom);
        btnTimeOfDayTo = (Button) findViewById(R.id.btnTimeOfDayTo);
        btnDateFrom = (Button) findViewById(R.id.btnDateFrom);
        btnDateTo = (Button) findViewById(R.id.btnDateTo);

        btnTimeOfDayFrom.setOnClickListener(this);
        btnTimeOfDayTo.setOnClickListener(this);
        btnDateFrom.setOnClickListener(this);
        btnDateTo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());

        switch(v.getId()) {

            case R.id.btnTimeOfDayFrom:
                TimePickerDialog tpFrom = new TimePickerDialog(mActivity, this,
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                tpFrom.show();
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ViewGroup vg = null;
        switch (buttonView.getId()) {
            case R.id.cbLimitDaysOfWeek:
                vg = vgLimitDaysOfWeek;
                break;
            case R.id.cbLimitTimeOfDay:
                vg = vgLimitTimeOfDay;
                break;
            case R.id.cbLimitDate:
                vg = vgLimitDate;
                break;
        }
        if (vg != null) {
            if (isChecked)
                vg.setVisibility(View.VISIBLE);
            else
                vg.setVisibility(View.GONE);
        }

    }
}

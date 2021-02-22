package johnrobert.delinila.jesusreigns.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import johnrobert.delinila.jesusreigns.R;
import johnrobert.delinila.jesusreigns.settings.DrawableUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarFragment extends Fragment {


    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        List<EventDay> events = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        events.add(new EventDay(calendar, DrawableUtils.getCircleDrawableWithText(getActivity(), "M")));

        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DAY_OF_MONTH, 10);
        events.add(new EventDay(calendar1, R.drawable.sample_icon_2));

        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.DAY_OF_MONTH, 10);
        events.add(new EventDay(calendar2, R.drawable.sample_icon_3, Color.parseColor("#228B22")));

        Calendar calendar3 = Calendar.getInstance();
        calendar3.add(Calendar.DAY_OF_MONTH, 7);
        events.add(new EventDay(calendar3, R.drawable.sample_four_icons));

        Calendar calendar4 = Calendar.getInstance();
        calendar4.add(Calendar.DAY_OF_MONTH, 13);
        events.add(new EventDay(calendar4, DrawableUtils.getThreeDots(getActivity())));

        CalendarView calendarView = view.findViewById(R.id.calendarView);

        Calendar min = Calendar.getInstance();
        min.add(Calendar.MONTH, -2);

        Calendar max = Calendar.getInstance();
        max.add(Calendar.MONTH, 2);

        calendarView.setMinimumDate(min);
        calendarView.setMaximumDate(max);

        calendarView.setEvents(events);

        calendarView.setDisabledDays(getDisabledDays());

        calendarView.setOnDayClickListener(eventDay ->
                Toast.makeText(getActivity().getApplicationContext(),
                        eventDay.getCalendar().getTime().toString() + " "
                                + eventDay.isEnabled(),
                        Toast.LENGTH_SHORT).show());


        return view;
    }

    private List<Calendar> getDisabledDays() {
        Calendar firstDisabled = DateUtils.getCalendar();
        firstDisabled.add(Calendar.DAY_OF_MONTH, 2);

        Calendar secondDisabled = DateUtils.getCalendar();
        secondDisabled.add(Calendar.DAY_OF_MONTH, 1);

        Calendar thirdDisabled = DateUtils.getCalendar();
        thirdDisabled.add(Calendar.DAY_OF_MONTH, 18);

        List<Calendar> calendars = new ArrayList<>();
        calendars.add(firstDisabled);
        calendars.add(secondDisabled);
        calendars.add(thirdDisabled);
        return calendars;
    }

}

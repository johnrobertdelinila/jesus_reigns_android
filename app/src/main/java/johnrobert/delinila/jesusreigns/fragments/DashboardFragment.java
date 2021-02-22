package johnrobert.delinila.jesusreigns.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.Random;

import johnrobert.delinila.jesusreigns.MainActivity;
import johnrobert.delinila.jesusreigns.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {


    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        CardView cardView  = view.findViewById(R.id.verse_background);

        cardView.setBackgroundResource(MainActivity.gradients[MainActivity.random]);
        return view;
    }

}

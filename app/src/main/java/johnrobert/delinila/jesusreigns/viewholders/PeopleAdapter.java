package johnrobert.delinila.jesusreigns.viewholders;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import johnrobert.delinila.jesusreigns.R;
import johnrobert.delinila.jesusreigns.objects.People;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleViewHolder> {

    private List<People> peopleList;
    private Activity activity;
    private List<People> clickedPeople;

    public PeopleAdapter(List<People> peopleList, Activity activity, List<People> clickedPeople) {
        this.peopleList = peopleList;
        this.activity = activity;
        this.clickedPeople = clickedPeople;
    }

    @NonNull
    @Override
    public PeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new PeopleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PeopleViewHolder holder, int position) {
        People people = peopleList.get(position);
        holder.setDetails(activity, people.getDisplayName(), people.getMinistry(), people.getPhotoURL());
        holder.itemView.setOnClickListener(v -> {
            if (!holder.checkBox.isChecked()) {
                clickedPeople.add(people);
                holder.checkBox.setChecked(true);
            }else {
                holder.checkBox.setChecked(false);
                removeClicked(people.getUid());
            }
        });
        holder.checkBox.setOnClickListener(v -> {
            if (!holder.checkBox.isChecked()) {
                removeClicked(people.getUid());
            }else {
                clickedPeople.add(people);
            }
        });
        if (isContains(people.getUid())) {
            holder.checkBox.setChecked(true);
        }

    }

    private boolean isContains(String uid) {
        for (People people: clickedPeople) {
            if (people.getUid().equalsIgnoreCase(uid)) {
                return true;
            }
        }
        return false;
    }

    private void removeClicked(String uid) {
        for (People people: clickedPeople) {
            if (people.getUid().equalsIgnoreCase(uid)) {
                clickedPeople.remove(people);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return peopleList.size();
    }

    public List<People> getClickPeople() {
        return this.clickedPeople;
    }

}

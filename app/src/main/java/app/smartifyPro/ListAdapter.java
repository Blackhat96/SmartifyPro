package app.smartifyPro;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    private final List<Adapter> List;

    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView hint;
        final ImageView img;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            hint = (TextView) view.findViewById(R.id.hint);
            img=(ImageView)view.findViewById(R.id.img);
        }
    }


    ListAdapter(List<Adapter> List) {
        this.List = List;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Adapter adapter = List.get(position);
        holder.title.setText(adapter.getTitle());
        holder.hint.setText(adapter.getHint());
    }

    @Override
    public int getItemCount() {
        return List.size();
    }
}
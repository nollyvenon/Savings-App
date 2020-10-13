package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.snowcorp.login.R;
import java.util.List;

import model.Customer;
import model.RecentActivity;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private Context context;
    private List<Customer> data;

    public CustomerAdapter(Context context, List<Customer> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Customer> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_customer_table, parent, false);
        return new CustomerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.user.setText(data.get(position).getFname() +" "+ data.get(position).getLname());
        holder.username.setText(data.get(position).getUsername());
        holder.email.setText(data.get(position).getEmail());
        holder.phone.setText(data.get(position).getPhone());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView user, username, email, phone;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            user = itemView.findViewById(R.id.client_name);
            user.setSingleLine(false);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            phone = itemView.findViewById(R.id.phone_number);
        }
    }
}
package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import model.SavingsTransaction;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SavingTransactionsAdapter extends RecyclerView.Adapter<SavingTransactionsAdapter.ViewHolder> {

    private Context context;
    private List<SavingsTransaction> data;
    private HashMap<String, String> user = new HashMap<>();

    public SavingTransactionsAdapter(Context context, List<SavingsTransaction> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<SavingsTransaction> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_savings_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        NumberFormat format = new DecimalFormat("#,###");

        holder.description.setText(data.get(position).getDescription());
        holder.source.setText(data.get(position).getSource());
        holder.type.setText(data.get(position).getType());
        holder.date.setText(data.get(position).getDate());
        holder.amount.setText("N" +format.format(Double.parseDouble(data.get(position).getAmount())));

    }

    private void updateLabel(EditText editText, Calendar calendar) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView description, source, type, date, amount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.description);
            source = itemView.findViewById(R.id.source);
            type = itemView.findViewById(R.id.type);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
        }
    }
}
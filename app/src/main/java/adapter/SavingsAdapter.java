package adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.johnnyup.erssavingsapp.HomeActivity;
import com.johnnyup.erssavingsapp.PaystackActivity;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import model.Savings;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.ViewHolder> {

    private Context context;
    private List<Savings> data;
    private HashMap<String, String> user = new HashMap<>();

    public SavingsAdapter(Context context, List<Savings> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Savings> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_savings_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        NumberFormat format = new DecimalFormat("#,###");
        String s = data.get(position).getSavings();
        Character firstLetter = s.charAt(0);
        holder.savingT.setText(R.string.savings);
        holder.savingV.setText(firstLetter.toString().toUpperCase() + s.substring(1, s.length()));

        holder.amountT.setText(R.string.amount);
        holder.amountV.setText(format.format(Double.parseDouble(data.get(position).getBalance())));

        holder.startDateT.setText(R.string.start_date);
        holder.startDateV.setText(data.get(position).getDate_start());

        holder.endDateT.setText(R.string.end_date);
        holder.endDateV.setText(data.get(position).getDate_end());

        holder.dailyAmountT.setText(R.string.daily_amount);
        holder.dailyAmountV.setText(format.format(Double.parseDouble(data.get(position).getAmount())));

        holder.statusT.setText(R.string.status);
        holder.statusV.setText(data.get(position).getStatus());

        holder.withdraw.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.item_withdraw, null);
            EditText amount = layout.findViewById(R.id.amount);

            builder.setView(layout);
            builder.setPositiveButton(R.string.withdraw, (dialog, id) -> {

                String amountStr = amount.getText().toString();
                if (amountStr.isEmpty()) {
                    Toast.makeText(context, "Amount is empty", Toast.LENGTH_LONG).show();
                    return;
                }

                if (Double.parseDouble(amountStr) > Double.parseDouble(data.get(position).getBalance())) {
                    Toast.makeText(context, "Your request is more than your savings", Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseHandler db = new DatabaseHandler(context);
                user = db.getUserDetails();
                Functions.savingsWithdraw(context, amountStr, user.get("uid"), String.valueOf(data.get(position).getId()));
            })
                    .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        holder.fund.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.item_fund, null);

            EditText amount = layout.findViewById(R.id.amount);
            amount.setText(data.get(position).getAmount());

            EditText myDate = layout.findViewById(R.id.date);

            final Calendar myCalendar = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener datePicker = (view, year, monthOfYear, dayOfMonth) -> {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myDate, myCalendar);
            };

            myDate.setOnClickListener(v1 -> {
                // TODO Auto-generated method stub
                new DatePickerDialog(context, datePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            builder.setView(layout);
            builder.setPositiveButton(R.string.start, (dialog, id) -> {
                String amountStr = amount.getText().toString();
                String dateStr = myDate.getText().toString().trim();

                if (amountStr.isEmpty()) {
                    Toast.makeText(context, "Amount is empty", Toast.LENGTH_LONG).show();
                    return;
                }

                if (dateStr.isEmpty()) {
                    Toast.makeText(context, "Pick date", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(context, PaystackActivity.class);
                intent.putExtra("amount", amountStr);
                intent.putExtra("date", dateStr);
                intent.putExtra("saving_id", String.valueOf(data.get(position).getId()));
                context.startActivity(intent);
            })
                    .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
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

        TextView savingT, savingV, amountT, amountV, startDateT, startDateV;
        TextView endDateT, endDateV, dailyAmountT, dailyAmountV, statusT, statusV;
        Button withdraw, fund;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            savingT = itemView.findViewById(R.id.header_title_saving);
            savingV = itemView.findViewById(R.id.header_value_saving);
            amountT = itemView.findViewById(R.id.header_title_amount);
            amountV = itemView.findViewById(R.id.header_value_amount);
            startDateT = itemView.findViewById(R.id.header_title_start_date);
            startDateV = itemView.findViewById(R.id.header_value_start_date);
            endDateT = itemView.findViewById(R.id.header_title_end_date);
            endDateV = itemView.findViewById(R.id.header_value_end_date);
            dailyAmountT = itemView.findViewById(R.id.header_title_daily_amount);
            dailyAmountV = itemView.findViewById(R.id.header_value_daily_amount);
            statusT = itemView.findViewById(R.id.header_title_status);
            statusV = itemView.findViewById(R.id.header_value_status);
            withdraw = itemView.findViewById(R.id.withdraw);
            fund = itemView.findViewById(R.id.fund);
        }
    }
}
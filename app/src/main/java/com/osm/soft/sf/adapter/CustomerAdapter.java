package com.osm.soft.sf.adapter;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.osm.soft.sf.CustomersActivity;
import com.osm.soft.sf.R;
import com.osm.soft.sf.model.Customer;
import com.osm.soft.sf.util.Functions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andres on 08/06/16.
 */
public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerHolder> implements Filterable {

    private List<Customer> mCustomers;
    private List<Customer> mCustomersFilter;
    private CustomersActivity customersActivity;

    public CustomerAdapter(final CustomersActivity customersActivity, final List<Customer> customers) {
        this.mCustomers = customers;
        this.customersActivity = customersActivity;
        mCustomersFilter = new ArrayList<>(customers);
    }

    public class CustomerHolder extends RecyclerView.ViewHolder {

        private TextView mTvName;
        private TextView mTvIdentification;
        private TextView mTvLetter;
        private ImageView mIvColor;
        private ImageView mIvSync;
        private View mIbEdit;
        private View mVActions;
        private View mTvOrder;

        public CustomerHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvIdentification = (TextView) itemView.findViewById(R.id.tv_identification);
            mTvLetter = (TextView) itemView.findViewById(R.id.tv_letter);
            mIvColor = (ImageView)itemView.findViewById(R.id.iv_color);
            mIvSync = (ImageView)itemView.findViewById(R.id.iv_sync);

            mVActions = itemView.findViewById(R.id.v_actions);
            mIbEdit = itemView.findViewById(R.id.ib_edit);
            mTvOrder = itemView.findViewById(R.id.tv_order);

            Functions.setViewSelected(mIbEdit);

            itemView.setOnClickListener(v -> {
                Customer customer = mCustomers.get(getAdapterPosition());
                if (customer.isView()) {
                    customer.setView(false);
                } else {
                    customer.setView(true);
                }
                mCustomers.set(getAdapterPosition(), customer);
                notifyItemChanged(getAdapterPosition());
            });

            mIbEdit.setOnClickListener(view -> CustomersActivity.goToCustomer(
                    itemView.getContext(), mCustomers.get(getAdapterPosition())));

            mTvOrder.setOnClickListener(view-> customersActivity.showOrder(mCustomers.get(getAdapterPosition())));
        }

        private void expand() {
            //set Visible
            mVActions.setVisibility(View.VISIBLE);

            final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            mVActions.measure(widthSpec, heightSpec);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                ValueAnimator mAnimator = slideAnimator(0, mVActions.getMeasuredHeight());
                mAnimator.start();
            }
        }

        private ValueAnimator slideAnimator(int start, int end) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                ValueAnimator animator = ValueAnimator.ofInt(start, end);

                animator.addUpdateListener(valueAnimator -> {
                    //Update Height
                    int value = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        value = (Integer) valueAnimator.getAnimatedValue();
                    }
                    ViewGroup.LayoutParams layoutParams = mVActions.getLayoutParams();
                    layoutParams.height = value;
                    mVActions.setLayoutParams(layoutParams);
                });
                return animator;
            }
            return null;
        }

        private void collapse() {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {

                ValueAnimator mAnimator = slideAnimator(mVActions.getHeight(), 0);
                if (mAnimator != null) {
                    mAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //Height=0, but it set visibility to GONE
                            mVActions.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });

                }

                mAnimator.start();
            } else {
                mVActions.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public CustomerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer,
                parent, false);

        return new CustomerHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomerHolder holder, int position) {
        Customer customer = mCustomers.get(position);
        holder.mTvName.setText(customer.getName());
        holder.mTvIdentification.setText(customer.getTin());
        if(customer.isSync())
            holder.mIvSync.setVisibility(View.INVISIBLE);
        else
            holder.mIvSync.setVisibility(View.VISIBLE);

        String index = String.valueOf(position);

        /*Functions.changeColor(holder.mIvColor, String.valueOf(customer.getName().charAt(0)),
                Integer.valueOf(index.substring(lenght - 1)), -1);*/
        GradientDrawable bgShape = (GradientDrawable) holder.mIvColor.getBackground();
        bgShape.setColor(Color.parseColor(Functions.getColor(Integer.valueOf(
                index.substring(index.length() - 1)))));
        holder.mTvLetter.setText(String.valueOf(customer.getName().charAt(0)));

        if(customer.isView())
            holder.expand();
        else
            holder.collapse();
    }

    @Override
    public int getItemCount() {
        return mCustomers.size();
    }

    @Override
    public Filter getFilter() {
        return new CustomerFilter(this, mCustomersFilter);
    }

    private static class CustomerFilter extends Filter{

        private List<Customer> filtersCustomers = new ArrayList<>();
        final private List<Customer> customers;
        final private CustomerAdapter customerAdapter;

        public CustomerFilter(CustomerAdapter customerAdapter, List<Customer> customers) {
            super();
            this.customers = customers;
            this.customerAdapter = customerAdapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filtersCustomers.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filtersCustomers.addAll(customers);
            } else {
                final String filterPattern = constraint.toString().toUpperCase().trim();

                for (final Customer customer : customers) {
                    if (customer.getName().toUpperCase().contains(filterPattern)
                            || customer.getCode().toUpperCase().contains(filterPattern)
                            || customer.getTin().contains(filterPattern)) {
                        filtersCustomers.add(customer);
                    }
                }
            }
            results.values = filtersCustomers;
            results.count = filtersCustomers.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            customerAdapter.mCustomers.clear();
            customerAdapter.mCustomers.addAll((ArrayList<Customer>) results.values);
            customerAdapter.notifyDataSetChanged();
        }

    }
}

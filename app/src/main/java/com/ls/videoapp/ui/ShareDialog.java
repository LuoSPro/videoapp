package com.ls.videoapp.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ls.libcommon.CornerFrameLayout;
import com.ls.videoapp.R;
import com.ls.videoapp.view.CustomImageView;

import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {

    List<ResolveInfo> shareitems = new ArrayList<>();
    private ShareAdapter shareAdapter;
    private String shareContent;
    private View.OnClickListener mListener;
    private CornerFrameLayout layout;

    protected ShareDialog(@NonNull Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //设置布局
        FrameLayout layout = new FrameLayout(getContext());
        RecyclerView gridView = new RecyclerView(getContext());
        layout.addView(gridView);
        setContentView(layout);
        //在底部显示
        getWindow().setGravity(Gravity.BOTTOM);

        //设置背景
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        queryShareItems();
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public void setShareItemClickListener(View.OnClickListener listener) {

        mListener = listener;
    }

    private void queryShareItems() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");


        List<ResolveInfo> resolveInfos = getContext().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName, "com.tencent.mm") || TextUtils.equals(packageName, "com.tencent.mobileqq")) {
                shareitems.add(resolveInfo);
            }
        }
        shareAdapter.notifyDataSetChanged();
    }

    private class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final PackageManager packageManager;

        public ShareAdapter() {
            packageManager = getContext().getPackageManager();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_item, parent, false);
            return new RecyclerView.ViewHolder(inflate) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ResolveInfo resolveInfo = shareitems.get(position);
            CustomImageView imageView = holder.itemView.findViewById(R.id.share_icon);
            Drawable drawable = resolveInfo.loadIcon(packageManager);
            imageView.setImageDrawable(drawable);

            TextView shareText = holder.itemView.findViewById(R.id.share_text);
            shareText.setText(resolveInfo.loadLabel(packageManager));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pkg = resolveInfo.activityInfo.packageName;
                    String cls = resolveInfo.activityInfo.name;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setComponent(new ComponentName(pkg, cls));
                    intent.putExtra(Intent.EXTRA_TEXT, shareContent);

                    getContext().startActivity(intent);

                    if (mListener != null) {
                        mListener.onClick(v);
                    }

                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return shareitems == null ? 0 : shareitems.size();
        }
    }
}


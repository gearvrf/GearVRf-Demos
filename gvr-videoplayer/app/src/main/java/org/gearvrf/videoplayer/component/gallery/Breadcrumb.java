package org.gearvrf.videoplayer.component.gallery;

import android.view.View;
import android.widget.TextView;

import org.gearvrf.videoplayer.R;

class Breadcrumb implements View.OnClickListener {

    private View mHome;
    private View mSource;
    private View mAlbum;
    private OnBreadcrumbListener onBreadcrumbListener;
    @SourceType
    private int mSourceType;

    public Breadcrumb(View mainView) {
        mHome = mainView.findViewById(R.id.home);
        mSource = mainView.findViewById(R.id.source);
        mAlbum = mainView.findViewById(R.id.album);
        mHome.setOnClickListener(this);
        showHome();
    }

    void showHome() {
        mHome.setBackgroundResource(R.drawable.bg_breadcrumb_home_normal);
        mSource.setVisibility(View.GONE);
        mAlbum.setVisibility(View.GONE);
    }

    void showSource(@SourceType int sourceType) {
        mSourceType = sourceType;
        mHome.setBackgroundResource(R.drawable.bg_breadcrumb_home_extended);
        mSource.setBackgroundResource(R.drawable.bg_breadcrumb_home_normal);
        mAlbum.setVisibility(View.GONE);
        mSource.setVisibility(View.VISIBLE);
        TextView label = mSource.findViewById(R.id.label);
        label.setText(getSourceLabel(sourceType));
    }

    void showAlbum(String albumTitle) {
        mSource.setBackgroundResource(R.drawable.bg_breadcrumb_source_extended);
        mAlbum.setBackgroundResource(R.drawable.bg_breadcrumb_source_normal);
        mAlbum.setVisibility(View.VISIBLE);
        TextView title = mAlbum.findViewById(R.id.title);
        title.setText(albumTitle);
    }

    public void setOnBreadcrumbListener(OnBreadcrumbListener onBreadcrumbListener) {
        this.onBreadcrumbListener = onBreadcrumbListener;
    }

    private CharSequence getSourceLabel(@SourceType int sourceType) {
        if (sourceType == SourceType.LOCAL) {
            return mHome.getContext().getString(R.string.gallery_source_type_local);
        } else {
            return mHome.getContext().getString(R.string.gallery_source_type_external);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.home) {
            if (onBreadcrumbListener != null) {
                showHome();
                onBreadcrumbListener.onHomeClicked();

            }
        } else if (view.getId() == R.id.source) {
            if (onBreadcrumbListener != null) {
                showSource(mSourceType);
                onBreadcrumbListener.onSourceClicked(mSourceType);

            }
        }
    }
}

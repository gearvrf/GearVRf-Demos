package org.gearvrf.videoplayer.component.gallery;

import android.view.View;
import android.widget.TextView;

import org.gearvrf.videoplayer.R;

class Breadcrumb implements View.OnClickListener {

    private View mHome;
    private View mAlbum;
    private OnBreadcrumbListener onBreadcrumbListener;

    public Breadcrumb(View mainView) {
        mHome = mainView.findViewById(R.id.home);
        mAlbum = mainView.findViewById(R.id.album);
        mHome.setOnClickListener(this);
    }

    void showHome() {
        mAlbum.setVisibility(View.GONE);
        mHome.setBackgroundResource(R.drawable.all_sides_rounded);
    }

    void showAlbum(String albumTitle) {
        mHome.setBackgroundResource(R.drawable.left_rounded);
        mAlbum.setVisibility(View.VISIBLE);
        TextView title = mAlbum.findViewById(R.id.title);
        title.setText(albumTitle);
    }

    public void setOnBreadcrumbListener(OnBreadcrumbListener onBreadcrumbListener) {
        this.onBreadcrumbListener = onBreadcrumbListener;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.home) {
            if (onBreadcrumbListener != null) {
                showHome();
                onBreadcrumbListener.onHomeClicked();

            }
        }
    }
}

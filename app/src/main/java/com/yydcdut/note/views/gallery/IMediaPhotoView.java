package com.yydcdut.note.views.gallery;

import com.yydcdut.note.entity.gallery.MediaFolder;
import com.yydcdut.note.model.gallery.SelectPhotoModel;
import com.yydcdut.note.views.IView;

import java.util.List;

/**
 * Created by yuyidong on 16/4/5.
 */
public interface IMediaPhotoView extends IView {

    void setListNavigationAdapter(List<String> folderNameList);

    void setMediaAdapter(MediaFolder mediaAdapter, SelectPhotoModel selectPhotoModel);

    void jump2PhotoDetail(int position, String folderName, boolean isPreviewSelected);

    void updateMediaFolder(MediaFolder mediaFolder);

    void setMenuTitle(String content);

    void notifyDataChanged();
}

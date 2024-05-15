package me.csxiong.uiux.ui.video.videocache.sourcestorage;

import me.csxiong.uiux.ui.video.videocache.SourceInfo;

/**
 * Storage for {@link SourceInfo}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public interface SourceInfoStorage {

    SourceInfo get(String url);

    void put(String url, SourceInfo sourceInfo);

    void release();
}

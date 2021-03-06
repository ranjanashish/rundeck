package com.dtolabs.rundeck.core.storage;


import com.dtolabs.rundeck.core.authorization.AuthContext;
import com.dtolabs.utils.Streams;
import org.rundeck.storage.api.ContentFactory;
import org.rundeck.storage.api.HasInputStream;
import org.rundeck.storage.api.Tree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Provides utility methods for use by the storage layer, or implementing plugins.
 */
public class StorageUtil {
    /**
     * Metadata key for the content-type
     */
    public static final String RES_META_RUNDECK_CONTENT_TYPE = "Rundeck-content-type";
    /**
     * Metadata key for the content size
     */
    public static final String RES_META_RUNDECK_CONTENT_LENGTH = "Rundeck-content-size";
    /**
     * Metadata key for the creation time
     */
    public static final String RES_META_RUNDECK_CONTENT_CREATION_TIME = "Rundeck-content-creation-time";
    /**
     * Metadata key for the modification time
     */
    public static final String RES_META_RUNDECK_CONTENT_MODIFY_TIME = "Rundeck-content-modify-time";
    /**
     * Date format for stored date/time
     */
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final ThreadLocal<DateFormat> w3cDateFormat = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            SimpleDateFormat fmt = new SimpleDateFormat(ISO_8601_FORMAT, Locale.US);
            fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            return fmt;
        }
    };

    /**
     * Return a factory for ResourceMeta
     * @return
     */
    public static ContentFactory<ResourceMeta> factory(){
        return new ContentFactory<ResourceMeta>() {
            @Override
            public ResourceMeta create(HasInputStream hasInputStream, Map<String, String> metadata) {
                return withStream(hasInputStream, metadata);
            }
        };
    }

    /**
     * Create a new builder
     *
     * @return builder
     */
    public static ResourceMetaBuilder create() {
        return create(null);
    }

    /**
     * Create a new builder with a set of metadata
     *
     * @param meta original metadata
     *
     * @return builder
     */
    public static ResourceMetaBuilder create(Map<String, String> meta) {
        ResourceMetaBuilder mutableRundeckResourceMeta = new ResourceMetaBuilder(meta);
        return mutableRundeckResourceMeta;
    }



    /**
     * Construct a resource
     *
     * @param stream
     * @param meta
     *
     * @return
     */
    public static ResourceMeta withStream(final HasInputStream stream, final Map<String, String> meta) {
        return new BaseStreamResource(meta,stream);
    }
    public static ResourceMeta withStream(final InputStream stream, final Map<String, String> meta) {
        return new BaseResource(meta) {
            @Override
            public long writeContent(OutputStream out) throws IOException {
                return Streams.copyStream(stream, out);
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return stream;
            }
        };
    }


    /**
     * Coerce a Tree of ResourceMeta into A StorageTree
     * @param impl the tree
     * @return a StorageTree
     */
    public static StorageTree asStorageTree(Tree<ResourceMeta> impl) {
        return new StorageTreeImpl(impl);
    }

    static long parseLong(String s, long defval) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {
        }
        return defval;
    }

    static Date parseDate(String s, Date defval) {
        try {
            return w3cDateFormat.get().parse(s);
        } catch (ParseException ignored) {
        }
        return defval;
    }

    public static String formatDate(Date time) {
        return w3cDateFormat.get().format(time);
    }

    /**
     * Create a StorageTree using authorization context and authorizing tree
     * @param context auth context
     * @param authStorage authorizing storage tree
     * @return StorageTree for the authorization context
     */
    public static StorageTree authorizedStorageTree(AuthContext context, AuthStorageTree authStorage) {
        return ContextStorageTree.with(context, authStorage);
    }
}

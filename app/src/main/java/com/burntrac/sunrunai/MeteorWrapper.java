package com.burntrac.sunrunai;

import java.util.HashMap;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;
import im.delight.android.ddp.db.Document;
import im.delight.android.ddp.db.Query;

/**
 * Created by kim on 9/2/17.
 */

public class MeteorWrapper {
    public static Meteor meteor;

    public final static int getCount(String collectionName) {
        if (meteor == null) {
            return 0;
        }

        Database database = meteor.getDatabase();

        if (database == null) {
            return 0;
        }

        Collection collection = database.getCollection(collectionName);

        return collection == null ? 0 : collection.count();
    }

    public final static Object findKVMatch(String collectionName, String key, Object value, String field) {
        Object result;

        synchronized (meteor) {
            Database database = meteor.getDatabase();
            Collection collection = database.getCollection(collectionName);

            Query query = collection.whereEqual(key, value);
            Document document = query.findOne();

            if (document == null) {
                return null;
            }

            if (field == null) {
                return document;
            }

            result = document.getField(field);
        }

        return result;
    }
}

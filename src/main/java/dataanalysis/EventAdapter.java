package dataanalysis;

/**
 * Adapter for parsing Event from array of strings
 */
public class EventAdapter implements StringArrayDataAdapter<Event> {
    @Override
    public Event adapt(String[] data) throws DataException {
        try {
            if (data == null) {
                throw new DataException("data array is null");
            }
            if (data.length != 2) {
                throw new DataException("incorrect data array length: " + data.length);
            }
            var uid = data[0].trim();
            if (uid.length() == 0) {
                throw new DataException("uid is empty");
            }
            var tag = data[1].trim();
            if (tag.length() == 0) {
                throw new DataException("tag is empty");
            }
            return new Event(uid, tag);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }
}

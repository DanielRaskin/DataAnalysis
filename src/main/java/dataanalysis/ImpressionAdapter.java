package dataanalysis;

/**
 * Adapter for parsing impression from array of string
 */
public class ImpressionAdapter implements StringArrayDataAdapter<Impression> {
    @Override
    public Impression adapt(String[] data) throws DataException {
        try {
            if (data == null) {
                throw new DataException("data array is null");
            }
            if (data.length != 10) {
                throw new DataException("incorrect data array length: " + data.length);
            }
            var timestamp = Utils.parseDateTime(data[0]);
            var uid = data[1].trim();
            if (uid.length() == 0) {
                throw new DataException("uid is empty");
            }
            var check = getArrayValue(data[2], Impression.Check.values());
            var timeCheck = getArrayValue(data[3], Impression.TimeCheck.values());
            var userTime = getArrayValue(data[4], Impression.UserTime.values());
            var dma = data[5].trim();
            var os = data[6].trim();
            var model = data[7].trim();
            var hardware = data[8].trim();
            var siteId = data[9].trim();

            return new Impression(timestamp, uid, check, timeCheck, userTime, dma, os, model, hardware, siteId);
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    private static <T> T getArrayValue(String s, T[] values) {
        var trimmed = s.trim();
        var i = (trimmed.length() == 0) ? null : Integer.parseInt(s);
        return ((i == null) || (i == -1)) ? null : values[i];
    }
}

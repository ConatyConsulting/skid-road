package io.ifar.skidroad.jdbi;

import io.ifar.skidroad.LogFile;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.sql.Timestamp;
import java.util.List;

/**
 *
 */
@RegisterMapper({DefaultJDBILogFileMapper.class, CountByStateMapper.class})
public interface DefaultJDBILogFileDAO extends JDBILogFileDAO {

    @Override
    @SqlUpdate("update log_files set state = :state, updated_at = :now where rolling_cohort = :rolling_cohort and serial = :serial and owner_uri = :owner")
    int updateState(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial, @Bind("state") String state, @Bind("owner") String expectedOwner, @Bind("now") Timestamp now);

    @Override
    @SqlQuery("select coalesce(max(serial),0) from log_files where rolling_cohort = :rolling_cohort")
    int determineNextSerial(@Bind("rolling_cohort") String rollingCohort);

    @Override
    @SqlUpdate("insert into log_files (rolling_cohort, serial, start_time, origin_uri, owner_uri, state, created_at) values (:rolling_cohort, :serial, :start_time, :origin_uri, :owner_uri, 'WRITING', :now)")
    int claimIndex(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial, @Bind("start_time") Timestamp startTime, @Bind("origin_uri") String originUri, @Bind("owner_uri") String ownerUri, @Bind("now") Timestamp now);

    @Override
    @SqlUpdate("update log_files set prep_uri = :prep_uri, updated_at = :now where rolling_cohort = :rolling_cohort and serial = :serial and owner_uri = :owner")
    int updatePrepPath(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial, @Bind("prep_uri") String prepUri, @Bind("owner") String expectedOwner, @Bind("now") Timestamp now);

    @Override
    @SqlUpdate("update log_files set archive_key = :archive_key, updated_at = :now where rolling_cohort = :rolling_cohort and serial = :serial and owner_uri = :owner")
    int updateArchiveKey(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial, @Bind("archive_key") String archiveKey, @Bind("owner") String expectedOwner, @Bind("now") Timestamp now);

    @Override
    @SqlUpdate("update log_files set archive_group = :archive_group, archive_uri = :archive_uri, updated_at = :now where rolling_cohort = :rolling_cohort and serial = :serial and owner_uri = :owner")
    int updateArchiveLocation(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial, @Bind("archive_group") String archiveGroup,@Bind("archive_uri") String archiveURI, @Bind("owner") String expectedOwner, @Bind("now") Timestamp now);

    @Override
    @SqlUpdate("update log_files set bytes = :bytes, updated_at = :now where rolling_cohort = :rolling_cohort and serial = :serial and owner_uri = :owner")
    int updateSize(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial, @Bind("bytes") Long byteSize, @Bind("owner") String expectedOwner, @Bind("now") Timestamp now);

    @Override
    @SqlQuery("select rolling_cohort, serial, start_time, origin_uri, prep_uri, archive_key, archive_uri, archive_group," +
            " state, owner_uri, bytes, created_at, updated_at from log_files where owner_uri = :owner_uri and state = :state order by start_time asc")
    ResultIterator<LogFile> findByOwnerAndState(@Bind("owner_uri") String ownerUri, @Bind("state") String state);

    @Override
    @SqlQuery("select distinct owner_uri from log_files")
    ResultIterator<String> listOwnerUris();

    @Override
    @SqlQuery("select rolling_cohort, serial, start_time, origin_uri, prep_uri, archive_key, archive_uri, archive_group," +
            " state, owner_uri, bytes, created_at, updated_at from log_files where state = :state and start_time >= :first_ts and start_time <= :last_ts" +
            " order by start_time asc")
    @FetchSize(50)
    ResultIterator<LogFile> listLogFilesByDateAndState(@Bind("state") String state, @Bind("first_ts") DateTime startDate,
                                                 @Bind("last_ts") DateTime endDate);

    @Override
    @SqlQuery("select rolling_cohort, serial, start_time, origin_uri, prep_uri, archive_key, archive_uri, archive_group," +
            " state, owner_uri, bytes, created_at, updated_at from log_files where start_time >= :first_ts and start_time <= :last_ts" +
            " order by start_time asc")
    @FetchSize(50)
    ResultIterator<LogFile> listLogFilesByDate(@Bind("first_ts") DateTime startDate,
                                               @Bind("last_ts") DateTime endDate);

    @Override
    @SqlQuery("select state, count(*) as cnt from log_files group by state order by cnt desc")
    List<CountByState> countLogFilesByState();

    @Override
    @SqlQuery("select count(*) as cnt from log_files where state = :state")
    int count(@Bind("state") String state);

    @Override
    @SqlQuery("select sum(bytes) from log_files where state = :state and start_time >= :first_ts and start_time <= :last_ts")
    Long totalSize(@Bind("state") String state, @Bind("first_ts") DateTime startDate,
                   @Bind("last_ts") DateTime endDate);

    @Override
    @SqlQuery("select count(*) from log_files where state = :state and start_time >= :first_ts and start_time <= :last_ts")
    int count(@Bind("state") String state, @Bind("first_ts") DateTime startDate,
               @Bind("last_ts") DateTime endDate);

    @Override
    @SqlQuery("select count(*) from log_files where start_time >= :first_ts and start_time <= :last_ts")
    int count(@Bind("first_ts") DateTime startDate,
               @Bind("last_ts") DateTime endDate);


    @Override
    @SqlQuery("select rolling_cohort, serial, start_time, origin_uri, prep_uri, archive_key, archive_uri, archive_group," +
            " state, owner_uri, bytes, created_at, updated_at from log_files where rolling_cohort = :rolling_cohort and serial = :serial")
    LogFile findByRollingCohortAndSerial(@Bind("rolling_cohort") String rollingCohort, @Bind("serial") int serial);

    @Override
    @SqlQuery("select rolling_cohort, serial, start_time, origin_uri, prep_uri, archive_key, archive_uri, archive_group," +
            " state, owner_uri, bytes, created_at, updated_at from log_files" +
            " where owner_uri = :owner, state = :state and start_time >= :first_ts and start_time <= :last_ts" +
            " order by start_time asc")
    @FetchSize(50)
    ResultIterator<LogFile> listLogFilesByOwnerAndDateAndState(@Bind("state") String state,
                                                               @Bind("owner") String owner,
                                                               @Bind("first_ts") DateTime startDate,
                                                               @Bind("last_ts") DateTime endDate);

    void close();
}

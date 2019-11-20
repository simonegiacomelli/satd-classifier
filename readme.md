Useful query


call csvwrite('~/Documents/tesi/dbsatds.csv', '
SELECT * FROM DBSATDS where satd_len<50 and fixed_len < 50 order by id desc
');

create table satd_by_pattern as
select pattern, count(*) as pattern_count FROM DBSATDS 
where satd_len<50 and fixed_len < 50 
group by pattern
order by count(*) desc;


call csvwrite('~/Documents/tesi/satd_by_pattern.csv'
,'select * from satd_by_pattern');
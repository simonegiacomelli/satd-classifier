Useful query


call csvwrite('~/Documents/tesi/dbsatds.csv', '

SELECT * FROM DBSATDS 
where 
  old_len<50 and new_len < 50 and clean_diff_ratio < 0.25
order by id desc

');

create table satd_by_pattern as
select pattern, count(*) as pattern_count FROM DBSATDS 
where old_len<50 and new_len < 50 and  clean_diff_ratio < 0.25
group by pattern
order by count(*) desc;


call csvwrite('~/Documents/tesi/satd_by_pattern.csv'
,'select * from satd_by_pattern');
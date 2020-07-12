insert into fisheye_ignored
  select csid
    from fisheye_revs t
   where t.csid like '%_release%'
     and not exists
   (select 1 from fisheye_ignored f where f.csid = t.csid);

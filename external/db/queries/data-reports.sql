--all AERs and corresponding DTTs
select distinct a.itemnumber aer_id,
                a.title      aer_title,
                a.owner      aer_owner,
                a.assignment aer_assigned,
                c.itemid     dtt_id,
                c.defectname dtt_title,
                c.aernumber  aer_number,
                c.reporter   dtt_reporter,
                c.assignment dtt_assigned
  from sharepoint_aer a
  left join aer_to_dtt_map b
    on a.id = b.aerid
  left join sharepoint_dtt c
    on c.id = b.dttid
 where a.team in ('Finance')
 order by 1;

--all non-AER dtts
select distinct c.itemid     dtt_id,
                c.defectname dtt_title,
                c.aernumber  aer_number,
                c.reporter   dtt_reporter,
                c.assignment dtt_assigned,
                c.team
  from sharepoint_dtt c
 where c.team in ('Finance')
   and not exists
 (select 1 from aer_to_dtt_map d where d.dttid = c.id)
 order by 1;

--DTTs with code changes
select distinct c.itemid     dtt_id,
                c.defectname dtt_title,
                c.aernumber  aer_number,
                c.reporter   dtt_reporter,
                c.assignment dtt_assigned,
                c.team
  from sharepoint_dtt c
 where c.team in ('Finance')
   and not exists (select 1 from aer_to_dtt_map d where d.dttid = c.id)
   and exists
 (select 1 from dtt_to_fisheye_map e where e.dttid = c.id)
 order by 1;

--DTTs with no code changes
select distinct c.itemid     dtt_id,
                c.defectname dtt_title,
                c.aernumber  aer_number,
                c.reporter   dtt_reporter,
                c.assignment dtt_assigned,
                c.team
  from sharepoint_dtt c
 where c.team in ('Finance')
   and not exists (select 1 from aer_to_dtt_map d where d.dttid = c.id)
   and not exists
 (select 1 from dtt_to_fisheye_map e where e.dttid = c.id)
 order by 1;

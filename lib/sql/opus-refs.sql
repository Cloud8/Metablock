
select distinct source_opus from sv_references
where source_opus > 0 -- 7400
  -- and uri like '%/eb/2010/02%'
  -- and uri != ''
  -- and source_opus < 4000
  order by source_opus
  limit <offset>, <count>

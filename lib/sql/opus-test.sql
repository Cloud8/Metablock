
select source_opus from statistics
where uri is not null
  and datediff(now(),date) < 5
  order by source_opus
  limit <offset>, <count>

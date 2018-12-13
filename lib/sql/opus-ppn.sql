
select s.source_opus, uri from statistics s, opus_autor o
where s.source_opus > 0 -- 6390
  and s.source_opus=o.source_opus
  and o.gnd is null
  and s.uri like '%/diss/z2014/____'
  order by s.source_opus
  limit <offset>, <count>

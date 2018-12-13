
-- series on
select s.source_opus from statistics s, opus_schriftenreihe os
 where s.source_opus > 0 -- 7400
  and s.source_opus = os.source_opus
  -- and os.sr_id = 20
  order by s.source_opus
  limit <offset>, <count>


-- series off : see OpusTransporter
select source_opus 
from statistics
where uri is not null
  -- and urn is not null
  and datediff(now(),date) < 7
  -- and date > '<date>'
  -- and source_opus > 0
  -- and doi is null
  and uri like '%/eb/2018/%'
  order by source_opus
  -- sql limit operator used by indexer
  limit <offset>, <count>

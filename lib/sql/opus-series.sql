
select sr_id 
from schriftenreihen
where url is not null
  and urn is not null
  limit <offset>, <count>

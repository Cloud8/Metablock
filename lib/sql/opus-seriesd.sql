
select s.sr_id, s.name, s.url, s.urn, s.doi, s.faculty, s.year, 
       f.name organization, u.universitaet, u.uni_gnd, u.instname, u.inst_gnd
from schriftenreihen s left join faculty f ON s.faculty=f.nr, university_de u
     where u.id=1
       and s.sr_id=<oid>
;

select s.uri, s.urn, s.doi, r.type, o.date_year year
from opus_schriftenreihe os, statistics s, resource_type r, opus o
where s.source_opus=os.source_opus
   and s.source_opus=o.source_opus
   and r.typeid=o.type
   and os.sr_id=<oid>
   order by o.date_year

;

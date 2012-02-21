-- oracle equivalent source sha1 08e0d2bf03a82ceaaf8c86c3826b30c9e5ac1c39

create or replace function rhn_ksscript_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_ksscript_mod_trig
before insert or update on rhnKickstartScript
for each row
execute procedure rhn_ksscript_mod_trig_fun();


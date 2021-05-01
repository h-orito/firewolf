insert into SKILL values ('WANDERER', '風来狩人', '風', 'VILLAGER', 8, 'あなたは風来狩人です。初日以外の毎晩、自分以外の一人を人狼の襲撃から守ることができます。ただし、護衛成功失敗にかかわらず、一度守った人は護衛することができません。');
insert into SKILL values ('LUCKYMAN', '強運者', '運', 'VILLAGER', 14, 'あなたは強運者です。特別な能力はありませんが、投票で最多票が複数いる場合、自分以外から処刑者が選ばれます。');

update SKILL set disp_order = 1 where skill_code = 'VILLAGER';
update SKILL set disp_order = 2 where skill_code = 'SEER';
update SKILL set disp_order = 3 where skill_code = 'WISE';
update SKILL set disp_order = 4 where skill_code = 'MEDIUM';
update SKILL set disp_order = 5 where skill_code = 'GURU';
update SKILL set disp_order = 6 where skill_code = 'CORONER';
update SKILL set disp_order = 7 where skill_code = 'HUNTER';
update SKILL set disp_order = 8 where skill_code = 'WANDERER';
update SKILL set disp_order = 9 where skill_code = 'MASON';
update SKILL set disp_order = 10 where skill_code = 'SYMPATHIZER';
update SKILL set disp_order = 11 where skill_code = 'STIGMATA';
update SKILL set disp_order = 12 where skill_code = 'MONSTERCAT';
update SKILL set disp_order = 13 where skill_code = 'BAKERY';
update SKILL set disp_order = 14 where skill_code = 'LUCKYMAN';
update SKILL set disp_order = 15 where skill_code = 'WEREWOLF';
update SKILL set disp_order = 16 where skill_code = 'CURSEWOLF';
update SKILL set disp_order = 17 where skill_code = 'WISEWOLF';
update SKILL set disp_order = 18 where skill_code = 'MADMAN';
update SKILL set disp_order = 19 where skill_code = 'CMADMAN';
update SKILL set disp_order = 20 where skill_code = 'FANATIC';
update SKILL set disp_order = 21 where skill_code = 'FOX';
update SKILL set disp_order = 22 where skill_code = 'LEFTOVER';
update SKILL set disp_order = 23 where skill_code = 'VILLAGERS';
update SKILL set disp_order = 24 where skill_code = 'WEREWOLFS';


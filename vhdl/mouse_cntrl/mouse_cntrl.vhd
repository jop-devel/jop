--
--	mouse.vhd
--
--	PS/2 mouse protocol
--


LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_arith.all;
USE ieee.numeric_std.all;

architecture behaviour of mouse_cntrl is
	constant enable  	  : std_logic_vector(9 downto 0) := "0111101000";	-- par,f4,start
	constant C_1US        : integer := CLK_FREQ/1000000;
	constant C_60US       : integer := CLK_FREQ/10000;

    constant TIMEOUT_1US  :std_logic_vector(TIMEOUT_REG_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(C_1US,TIMEOUT_REG_WIDTH));
    constant TIMEOUT_60US :std_logic_vector(TIMEOUT_REG_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(C_60US,TIMEOUT_REG_WIDTH));
	
	type mouse_state_type is ( start, init, wait_l, wait_h, read_bytes,
							   read_l, read_h, data_rdy );
	signal mouse_state	: mouse_state_type;

	signal clk_out		: std_logic;
	signal clk_shr		: std_logic_vector(2 downto 0);
	signal data_shr	    : std_logic_vector(2 downto 0);
	signal clk_buf		: std_logic;
	signal data_buf	    : std_logic;

	signal inh_cnt		: std_logic_vector(TIMEOUT_REG_WIDTH-1 downto 0);
	signal out_cnt		: std_logic_vector(4 downto 0);
	signal in_cnt		: std_logic_vector(5 downto 0);
	signal sho		    : std_logic_vector(9 downto 0);
	signal shi		    : std_logic_vector(32 downto 0);

    signal rdy          : std_logic;
	signal button       : std_logic_vector(2 downto 0);
    signal x            : std_logic_vector(7 downto 0);
    signal y            : std_logic_vector(7 downto 0);
    signal xsig         : std_logic;
    signal ysig         : std_logic;
    signal xovl         : std_logic;
    signal yovl         : std_logic;
    
    signal flag_reg      : std_logic_vector(31 downto 0);
    signal flag_reg_next : std_logic_vector(31 downto 0);
    
    signal rst_req      : std_logic;
    
-- Funktionen, die das Leben etwas erleichtern

    function Extend (X: Std_Logic_Vector; LENGTH : positive) 
      return Std_Logic_Vector is
    begin
      return IEEE.std_logic_arith.ext(X,LENGTH);
    end;


    function Max(LEFT, RIGHT : integer) return integer is
    begin
        if left > right then
            return left;
        else
            return right;
        end if;
    end;
begin


	ps2_data <= 'Z' when sho(0)='1' else '0';		-- a simple open drain
	ps2_clk <= 'Z' when clk_out='1' else '0';

    rdy_cnt <= (others => '0');
    
  simpcon: process( clk, rst )
    variable xval : std_logic_vector(31 downto 0);
    variable yval : std_logic_vector(31 downto 0);
  begin
    if( rst = '1' ) then
      flag_reg_next <= (others => '0');
      rst_req       <= '0';
    elsif( clk'event and clk = '1' ) then           
      flag_reg_next <= (others => '0');
      rst_req       <= '0';
      if(rdy = '1' or flag_reg(0) = '1') then
        flag_reg_next(0) <= '1';
      end if;
      flag_reg_next(3 downto 1) <= button;
      flag_reg_next(4) <= xovl;
      flag_reg_next(5) <= yovl;
      flag_reg <= flag_reg_next;
      
      if( xsig = '0' ) then
        xval(31 downto 8) := (others => '0');
      else
        xval(31 downto 8) := (others => '1');
      end if;
      xval(7 downto 0) := x;
            
      if( ysig = '0' ) then
        yval(31 downto 8) := (others => '0');
      else
        yval(31 downto 8) := (others => '1');
      end if;
      yval(7 downto 0) := y;
      
      -- Data read requested from the simpcon interface.
      if(rd = '1') then
        -- Read request to the status register.
        if(address(1 downto 0) = B"00") then
          rd_data <= (others => '0');
        -- Read request to the flag register.
        elsif(address(1 downto 0) = B"01") then
          rd_data <= flag_reg;
          -- Reset new data flag.
          flag_reg_next(0) <= '0';
        -- Read request to the x-change register.
        elsif(address(1 downto 0) = B"10") then
          rd_data <= xval;
        -- Read request to the y-change register.
        elsif(address(1 downto 0) = B"11") then
          rd_data <= yval;
        end if;
      end if;
      
      -- Data write requested from the simpcon interface.
      if(wr = '1') then
        rst_req       <= '1';
      end if;
      
      -- Set interrupt flag.
      int_flg <= rdy;
    end if;
  end process;
  
--
--	sync in mouse clk, data and filter glitches
--
process(clk)
begin
	if clk'event and clk='1' then
		clk_shr(0) <= ps2_clk;
		clk_shr(2 downto 1) <= clk_shr(1 downto 0);
		data_shr(0) <= ps2_data;
		data_shr(2 downto 1) <= data_shr(1 downto 0);

		if clk_shr = "000" then
			clk_buf <= '0';
		elsif clk_shr = "111" then
			clk_buf <= '1';
		end if;

		if data_shr = "000" then
			data_buf <= '0';
		elsif data_shr = "111" then
			data_buf <= '1';
		end if;

	end if;
end process;

--
--	do this 'dirty' protocol
--	its like IIC, but not really ( no cash for Philips )
--
process(clk, rst)
begin
	if clk'event and clk='1' then

		if rst='1' then
			mouse_state <= start;
			button <= (others => '0' );
			x <= (others => '0' );
			y <= (others => '0' );
			rdy <= '0';
			sho <= (others => '1' );
			inh_cnt <= (others => '0' );
			clk_out <= '0';
		else
		    if( rst_req = '1' ) then
		      mouse_state <= start;
		    end if;
		    
			case mouse_state is

				when start =>
					if inh_cnt=TIMEOUT_60US then		-- >60us clk low => reset device
						mouse_state <= init;
						sho <= enable;
						inh_cnt <= (others => '0');
					else
						inh_cnt <= inh_cnt + 1;
					end if;

				when init =>
					clk_out <= '1';
					out_cnt <= (others => '0');
					if inh_cnt=TIMEOUT_1US then		-- wait > 1us to not see my low clock
						mouse_state <= wait_l;
					else
						inh_cnt <= inh_cnt + 1;
					end if;

				when wait_l =>
					if out_cnt="10110" then			-- one byte out, one in
						mouse_state <= read_bytes;
					else
						if clk_buf='0' then
							mouse_state <= wait_h;
							sho(9) <= '1';
							sho(8 downto 0) <= sho(9 downto 1);
						end if;
					end if;

				when wait_h =>
					if clk_buf='1' then
						mouse_state <= wait_l;

						out_cnt <= out_cnt + 1;
					end if;

				when read_bytes =>
					rdy <= '0';
					in_cnt <= (others => '0');
					mouse_state <= read_l;

				when read_l =>
					if in_cnt="100001" then		-- one mouse msg is 33 clocks
						mouse_state <= data_rdy;
					else
						if clk_buf='0' then
							shi(32) <= data_buf;
							shi(31 downto 0) <= shi(32 downto 1);
							mouse_state <= read_h;
						end if;
					end if;

				when read_h =>
					if clk_buf='1' then
						mouse_state <= read_l;

						in_cnt <= in_cnt + 1;
					end if;

				when data_rdy =>
					mouse_state <= read_bytes;

					button <= shi(3 downto 1);
					x <= shi(19 downto 12);
					y <= shi(30 downto 23);
					xsig <= shi(5);
					ysig <= shi(6);
					xovl <= shi(7);
					yovl <= shi(8);
					rdy <= '1';

			end case;
		end if;
	end if;
end process;


end behaviour;